package com.weintern.inventory.service;

import com.weintern.inventory.domain.*;
import com.weintern.inventory.dto.PurchaseOrderForm;
import com.weintern.inventory.dto.PurchaseOrderItemForm;
import com.weintern.inventory.exception.BusinessException;
import com.weintern.inventory.exception.ResourceNotFoundException;
import com.weintern.inventory.repository.ProductRepository;
import com.weintern.inventory.repository.PurchaseOrderRepository;
import com.weintern.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Purchase-order lifecycle: DRAFT -> ORDERED -> PARTIALLY_RECEIVED -> RECEIVED (or CANCELLED before any receipt).
 * Receiving locks the order, validates every line, then adds stock through {@link InventoryService}
 * inside one transaction, so a failed receipt changes nothing.
 */
@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    public static final int PAGE_SIZE = 10;

    private final PurchaseOrderRepository orderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public Page<PurchaseOrder> list(PoStatus status, int page) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Order.desc("id")));
        return status == null ? orderRepository.findAll(pageable) : orderRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public PurchaseOrder get(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Purchase order", id));
    }

    @Transactional
    public PurchaseOrder createDraft(PurchaseOrderForm form) {
        Supplier supplier = supplierRepository.findById(form.getSupplierId())
                .orElseThrow(() -> new BusinessException("The selected supplier does not exist."));
        if (!supplier.isActive()) throw new BusinessException("Supplier " + supplier.getName() + " is inactive.");

        PurchaseOrder po = new PurchaseOrder();
        po.setOrderNumber("TMP-" + UUID.randomUUID());
        po.setSupplier(supplier);
        po.setExpectedDate(form.getExpectedDate());
        po.setNotes(form.getNotes() == null || form.getNotes().isBlank() ? null : form.getNotes().trim());
        po.setCreatedBy(InventoryService.currentUsername());
        po = orderRepository.save(po);
        po.setOrderNumber(String.format("PO-%05d", po.getId()));
        return po;
    }

    @Transactional
    public PurchaseOrder addItem(Long poId, PurchaseOrderItemForm form) {
        PurchaseOrder po = lock(poId);
        requireStatus(po, "add items to", PoStatus.DRAFT);
        Product product = productRepository.findById(form.getProductId())
                .orElseThrow(() -> new BusinessException("The selected product does not exist."));
        if (!product.isActive()) throw new BusinessException("Product " + product.getSku() + " is archived.");

        var existing = po.getItems().stream().filter(i -> i.getProduct().getId().equals(product.getId())).findFirst();
        if (existing.isPresent()) {
            int total = existing.get().getQuantityOrdered() + form.getQuantity();
            if (total > InventoryService.MAX_QUANTITY) throw new BusinessException("Quantity is too large.");
            existing.get().setQuantityOrdered(total);
        } else {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setProduct(product);
            item.setQuantityOrdered(form.getQuantity());
            item.setUnitCost(form.getUnitCost() != null ? form.getUnitCost() : product.getCostPrice());
            po.addItem(item);
        }
        return po;
    }

    @Transactional
    public PurchaseOrder removeItem(Long poId, Long itemId) {
        PurchaseOrder po = lock(poId);
        requireStatus(po, "remove items from", PoStatus.DRAFT);
        if (!po.getItems().removeIf(i -> i.getId().equals(itemId))) {
            throw new ResourceNotFoundException("Order line", itemId);
        }
        return po;
    }

    @Transactional
    public PurchaseOrder place(Long poId) {
        PurchaseOrder po = lock(poId);
        requireStatus(po, "place", PoStatus.DRAFT);
        if (po.getItems().isEmpty()) throw new BusinessException("Add at least one item before placing the order.");
        po.setStatus(PoStatus.ORDERED);
        po.setOrderedAt(LocalDateTime.now());
        return po;
    }

    @Transactional
    public PurchaseOrder cancel(Long poId) {
        PurchaseOrder po = lock(poId);
        if (po.getStatus() != PoStatus.DRAFT && po.getStatus() != PoStatus.ORDERED) {
            throw new BusinessException("Only draft or ordered purchase orders can be cancelled.");
        }
        if (po.getTotalReceived() > 0) throw new BusinessException("This order already has receipts and cannot be cancelled.");
        po.setStatus(PoStatus.CANCELLED);
        po.setCompletedAt(LocalDateTime.now());
        return po;
    }

    /**
     * Records a delivery. {@code quantities} maps order-line id to units received now.
     * All-or-nothing: if any line is invalid the whole receipt is rejected.
     */
    @Transactional
    public PurchaseOrder receive(Long poId, Map<Long, Integer> quantities) {
        PurchaseOrder po = lock(poId);
        requireStatus(po, "receive", PoStatus.ORDERED, PoStatus.PARTIALLY_RECEIVED);

        boolean anyReceived = false;
        for (PurchaseOrderItem item : po.getItems()) {
            Integer qty = quantities.get(item.getId());
            if (qty == null || qty == 0) continue;
            if (qty < 0) throw new BusinessException("Received quantity cannot be negative.");
            if (qty > item.getOutstanding()) {
                throw new BusinessException("Cannot receive " + qty + " of " + item.getProduct().getSku() + ": only "
                        + item.getOutstanding() + " still outstanding on " + po.getOrderNumber() + ".");
            }
            item.setQuantityReceived(item.getQuantityReceived() + qty);
            inventoryService.receiveStock(item.getProduct().getId(), qty,
                    "Received against " + po.getOrderNumber(), po.getOrderNumber());
            anyReceived = true;
        }
        if (!anyReceived) throw new BusinessException("Enter a received quantity for at least one item.");

        if (po.getTotalOutstanding() == 0) {
            po.setStatus(PoStatus.RECEIVED);
            po.setCompletedAt(LocalDateTime.now());
        } else {
            po.setStatus(PoStatus.PARTIALLY_RECEIVED);
        }
        return po;
    }

    private PurchaseOrder lock(Long id) {
        return orderRepository.findByIdForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("Purchase order", id));
    }

    private void requireStatus(PurchaseOrder po, String action, PoStatus... allowed) {
        for (PoStatus s : allowed) if (po.getStatus() == s) return;
        throw new BusinessException("You cannot " + action + " " + po.getOrderNumber() + " while it is "
                + po.getStatus().getLabel().toLowerCase() + ".");
    }
}
