package com.foresight.inventory;

import com.foresight.inventory.entity.*;
import com.foresight.inventory.repository.ProductRepository;
import com.foresight.inventory.repository.StockLocationRepository;
import com.foresight.inventory.repository.StockMovementRepository;
import com.foresight.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockLocationRepository stockLocationRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Product sampleProduct;
    private StockLocation sampleLocation;

    @BeforeEach
    void setUp() {
        sampleLocation = new StockLocation("WH-01", "Main Warehouse", "MAIN", "123 Industrial Way");
        sampleLocation.setId(1L);

        Category category = new Category("Electronics", "ELEC", "Test Category");
        category.setId(1L);

        sampleProduct = new Product(
                "PRD-TEST", "Test IoT Controller", "Testing description",
                category, null, sampleLocation, "PCS",
                new BigDecimal("50.00"), new BigDecimal("90.00"),
                25, 10, 100, null
        );
        sampleProduct.setId(10L);
    }

    @Test
    void testStockInMovementIncreasesBalance() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(sampleProduct));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        StockMovement movement = inventoryService.recordStockMovement(
                10L, 1L, MovementType.STOCK_IN, 15, "Supplier restock", "REF-100", "tester"
        );

        assertNotNull(movement);
        assertEquals(40, sampleProduct.getCurrentStock());
        assertEquals(25, movement.getPreviousStock());
        assertEquals(40, movement.getNewStock());
        assertEquals(15, movement.getQuantityChange());
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    void testStockOutReducesBalance() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(sampleProduct));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

        StockMovement movement = inventoryService.recordStockMovement(
                10L, 1L, MovementType.STOCK_OUT, -10, "Sales dispatch", "DISP-55", "tester"
        );

        assertEquals(15, sampleProduct.getCurrentStock());
        assertEquals(25, movement.getPreviousStock());
        assertEquals(15, movement.getNewStock());
    }

    @Test
    void testNegativeStockReductionThrowsException() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(sampleProduct));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.recordStockMovement(
                    10L, 1L, MovementType.STOCK_OUT, -50, "Excessive dispatch", "ERR-01", "tester"
            );
        });

        assertTrue(ex.getMessage().contains("Cannot reduce stock below zero"));
        assertEquals(25, sampleProduct.getCurrentStock()); // Stock remains untouched
    }
}
