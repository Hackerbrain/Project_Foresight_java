package com.weintern.inventory;

import com.weintern.inventory.domain.*;
import com.weintern.inventory.dto.*;
import com.weintern.inventory.exception.BusinessException;
import com.weintern.inventory.repository.ProductRepository;
import com.weintern.inventory.repository.StockMovementRepository;
import com.weintern.inventory.service.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Integration test against an in-memory database: the handbook's "8 units, order 40, 25 arrive" scenario. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PurchaseOrderServiceTest {

    @Autowired PurchaseOrderService orders;
    @Autowired ProductService products;
    @Autowired SupplierService suppliers;
    @Autowired CategoryService categories;
    @Autowired ProductRepository productRepository;
    @Autowired StockMovementRepository movementRepository;
    @Autowired EntityManager em;

    private Product product;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        Category category = categories.create("Electronics", null);
        ProductForm pf = new ProductForm();
        pf.setSku("el-1001");
        pf.setName("Wireless barcode scanner");
        pf.setCategoryId(category.getId());
        pf.setUnit("pcs");
        pf.setCostPrice(new BigDecimal("1850.00"));
        pf.setSellingPrice(new BigDecimal("2499.00"));
        pf.setReorderLevel(10);
        pf.setOpeningStock(8);
        product = products.create(pf);

        SupplierForm sf = new SupplierForm();
        sf.setName("TechSource");
        sf.setProductIds(List.of(product.getId()));
        supplier = suppliers.create(sf);
    }

    private PurchaseOrder draftWithItem(int quantity) {
        PurchaseOrderForm form = new PurchaseOrderForm();
        form.setSupplierId(supplier.getId());
        PurchaseOrder po = orders.createDraft(form);
        PurchaseOrderItemForm item = new PurchaseOrderItemForm();
        item.setProductId(product.getId());
        item.setQuantity(quantity);
        orders.addItem(po.getId(), item);
        em.flush();
        return po;
    }

    private PurchaseOrder orderedPo(int quantity) {
        PurchaseOrder po = draftWithItem(quantity);
        orders.place(po.getId());
        return po;
    }

    private int stock() {
        return productRepository.findById(product.getId()).orElseThrow().getQuantityOnHand();
    }

    @Test
    void openingStockCreatesLedgerEntryAndProductIsLowStock() {
        assertThat(stock()).isEqualTo(8);
        assertThat(product.isLowStock()).isTrue();
        assertThat(product.getSku()).isEqualTo("EL-1001");
        assertThat(movementRepository.findTop20ByProductIdOrderByCreatedAtDescIdDesc(product.getId()))
                .extracting(StockMovement::getReason).contains("Opening stock");
    }

    @Test
    void partialThenFullReceiptUpdatesStockAndStatus() {
        PurchaseOrder po = orderedPo(40);
        Long itemId = po.getItems().get(0).getId();
        assertThat(po.getOrderNumber()).startsWith("PO-");

        orders.receive(po.getId(), Map.of(itemId, 25));

        assertThat(stock()).isEqualTo(33);
        assertThat(po.getStatus()).isEqualTo(PoStatus.PARTIALLY_RECEIVED);
        assertThat(po.getTotalOutstanding()).isEqualTo(15);
        StockMovement latest = movementRepository.findTop20ByProductIdOrderByCreatedAtDescIdDesc(product.getId()).get(0);
        assertThat(latest.getType()).isEqualTo(MovementType.PO_RECEIPT);
        assertThat(latest.getQuantityChange()).isEqualTo(25);
        assertThat(latest.getBalanceAfter()).isEqualTo(33);
        assertThat(latest.getReference()).isEqualTo(po.getOrderNumber());

        orders.receive(po.getId(), Map.of(itemId, 15));

        assertThat(stock()).isEqualTo(48);
        assertThat(po.getStatus()).isEqualTo(PoStatus.RECEIVED);
        assertThat(po.getCompletedAt()).isNotNull();
    }

    @Test
    void receivingMoreThanOutstandingIsRejectedAndStockIsUnchanged() {
        PurchaseOrder po = orderedPo(40);
        Long itemId = po.getItems().get(0).getId();

        assertThatThrownBy(() -> orders.receive(po.getId(), Map.of(itemId, 41)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("outstanding");

        assertThat(stock()).isEqualTo(8);
        assertThat(po.getStatus()).isEqualTo(PoStatus.ORDERED);
    }

    @Test
    void receiptWithNoQuantitiesIsRejected() {
        PurchaseOrder po = orderedPo(10);
        Long itemId = po.getItems().get(0).getId();
        assertThatThrownBy(() -> orders.receive(po.getId(), Map.of(itemId, 0)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void draftCannotBeReceivedAndEmptyDraftCannotBePlaced() {
        PurchaseOrder draft = draftWithItem(5);
        Long itemId = draft.getItems().get(0).getId();
        assertThatThrownBy(() -> orders.receive(draft.getId(), Map.of(itemId, 1)))
                .isInstanceOf(BusinessException.class);

        PurchaseOrderForm form = new PurchaseOrderForm();
        form.setSupplierId(supplier.getId());
        PurchaseOrder empty = orders.createDraft(form);
        assertThatThrownBy(() -> orders.place(empty.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("at least one item");
    }

    @Test
    void addingSameProductTwiceMergesQuantities() {
        PurchaseOrder po = draftWithItem(10);
        PurchaseOrderItemForm again = new PurchaseOrderItemForm();
        again.setProductId(product.getId());
        again.setQuantity(5);
        orders.addItem(po.getId(), again);
        assertThat(po.getItems()).hasSize(1);
        assertThat(po.getItems().get(0).getQuantityOrdered()).isEqualTo(15);
        assertThat(po.getTotalAmount()).isEqualByComparingTo("27750.00");
    }

    @Test
    void ordersWithReceiptsCannotBeCancelled() {
        PurchaseOrder po = orderedPo(10);
        Long itemId = po.getItems().get(0).getId();
        orders.receive(po.getId(), Map.of(itemId, 4));
        assertThatThrownBy(() -> orders.cancel(po.getId())).isInstanceOf(BusinessException.class);
    }

    @Test
    void orderedPurchaseOrderCanBeCancelledBeforeAnyReceipt() {
        PurchaseOrder po = orderedPo(10);
        orders.cancel(po.getId());
        assertThat(po.getStatus()).isEqualTo(PoStatus.CANCELLED);
    }
}
