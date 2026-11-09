package com.weintern.inventory;

import com.weintern.inventory.domain.MovementType;
import com.weintern.inventory.domain.Product;
import com.weintern.inventory.domain.StockMovement;
import com.weintern.inventory.exception.BusinessException;
import com.weintern.inventory.repository.ProductRepository;
import com.weintern.inventory.repository.StockMovementRepository;
import com.weintern.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock ProductRepository productRepository;
    @Mock StockMovementRepository movementRepository;
    @InjectMocks InventoryService service;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setSku("EL-1001");
        product.setQuantityOnHand(25);
        product.setReorderLevel(10);
        product.setActive(true);
    }

    private void stubProductAndSave() {
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void stockInIncreasesQuantityAndWritesLedgerEntry() {
        stubProductAndSave();

        StockMovement m = service.stockIn(1L, 5, "Supplier delivery", "INV-9");

        assertThat(product.getQuantityOnHand()).isEqualTo(30);
        assertThat(m.getType()).isEqualTo(MovementType.STOCK_IN);
        assertThat(m.getQuantityChange()).isEqualTo(5);
        assertThat(m.getBalanceAfter()).isEqualTo(30);
        assertThat(m.getReference()).isEqualTo("INV-9");
        assertThat(m.getPerformedBy()).isEqualTo("system");
    }

    @Test
    void stockOutThatDropsBelowReorderLevelFlagsLowStock() {
        stubProductAndSave();
        assertThat(product.isLowStock()).isFalse();

        service.stockOut(1L, 17, "Customer sale", null);

        assertThat(product.getQuantityOnHand()).isEqualTo(8);
        assertThat(product.isLowStock()).isTrue();
    }

    @Test
    void stockOutBeyondAvailableIsRejectedAndNothingIsRecorded() {
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.stockOut(1L, 26, "Too many", null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Not enough stock");

        assertThat(product.getQuantityOnHand()).isEqualTo(25);
        verify(movementRepository, never()).save(any());
    }

    @Test
    void adjustmentSetsCountedQuantityAndRecordsSignedDifference() {
        stubProductAndSave();

        StockMovement m = service.adjust(1L, 20, "Stock count");

        assertThat(product.getQuantityOnHand()).isEqualTo(20);
        assertThat(m.getType()).isEqualTo(MovementType.ADJUSTMENT);
        assertThat(m.getQuantityChange()).isEqualTo(-5);
    }

    @Test
    void adjustmentToSameQuantityIsRejected() {
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        assertThatThrownBy(() -> service.adjust(1L, 25, "Recount"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void zeroOrNegativeQuantitiesAreRejected() {
        assertThatThrownBy(() -> service.stockIn(1L, 0, "x", null)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.stockOut(1L, -3, "x", null)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.adjust(1L, -1, "x")).isInstanceOf(BusinessException.class);
    }

    @Test
    void reasonIsMandatory() {
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        assertThatThrownBy(() -> service.stockIn(1L, 1, "  ", null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("reason");
    }

    @Test
    void archivedProductsCannotBeChanged() {
        product.setActive(false);
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        assertThatThrownBy(() -> service.stockIn(1L, 1, "x", null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("archived");
    }

    @Test
    void lowStockIsBasedOnReorderLevel() {
        product.setQuantityOnHand(11);
        assertThat(product.isLowStock()).isFalse();
        product.setQuantityOnHand(10);
        assertThat(product.isLowStock()).isTrue();
        product.setQuantityOnHand(0);
        assertThat(product.isLowStock()).isTrue();
    }
}
