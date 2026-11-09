package com.foresight.inventory.service;

import com.foresight.inventory.dto.PurchaseOrderItemRequest;
import com.foresight.inventory.dto.PurchaseOrderRequest;
import com.foresight.inventory.dto.ReceiveItemRequest;
import com.foresight.inventory.entity.*;
import com.foresight.inventory.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final SupplierRepository supplierRepository;
    private final StockLocationRepository stockLocationRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                PurchaseOrderItemRepository purchaseOrderItemRepository,
                                SupplierRepository supplierRepository,
                                StockLocationRepository stockLocationRepository,
                                ProductRepository productRepository,
                                InventoryService inventoryService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.supplierRepository = supplierRepository;
        this.stockLocationRepository = stockLocationRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getAllOrders() {
        return purchaseOrderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public PurchaseOrder getOrderById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase order not found with id: " + id));
    }

    @Transactional
    public PurchaseOrder createOrder(PurchaseOrderRequest request, String createdBy) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found with id: " + request.getSupplierId()));

        StockLocation targetLocation = null;
        if (request.getTargetLocationId() != null) {
            targetLocation = stockLocationRepository.findById(request.getTargetLocationId()).orElse(null);
        } else {
            targetLocation = stockLocationRepository.findAll().stream().findFirst().orElse(null);
        }

        String poNumber = "PO-" + System.currentTimeMillis() % 1000000;
        PurchaseOrder po = new PurchaseOrder(poNumber, supplier, targetLocation, request.getNotes(), createdBy);

        for (PurchaseOrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + itemReq.getProductId()));

            PurchaseOrderItem item = new PurchaseOrderItem(
                    po,
                    product,
                    itemReq.getQuantity(),
                    itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : product.getCostPrice()
            );
            po.addItem(item);
        }

        return purchaseOrderRepository.save(po);
    }

    @Transactional
    public PurchaseOrder markAsOrdered(Long id) {
        PurchaseOrder po = getOrderById(id);
        if (po.getStatus() != OrderStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT orders can be moved to ORDERED status");
        }
        po.setStatus(OrderStatus.ORDERED);
        po.setUpdatedAt(LocalDateTime.now());
        return purchaseOrderRepository.save(po);
    }

    @Transactional
    public PurchaseOrder receiveItems(Long poId, List<ReceiveItemRequest> receipts, String receivedBy) {
        PurchaseOrder po = getOrderById(poId);

        if (po.getStatus() == OrderStatus.CANCELLED || po.getStatus() == OrderStatus.RECEIVED) {
            throw new IllegalStateException("Cannot receive items for order in status: " + po.getStatus());
        }

        for (ReceiveItemRequest receipt : receipts) {
            PurchaseOrderItem item = purchaseOrderItemRepository.findById(receipt.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Order line item not found: " + receipt.getItemId()));

            if (!item.getPurchaseOrder().getId().equals(poId)) {
                throw new IllegalArgumentException("Line item does not belong to this purchase order");
            }

            int remaining = item.getRemainingQuantity();
            int qtyToReceive = receipt.getQuantityReceived();

            if (qtyToReceive > remaining) {
                throw new IllegalArgumentException("Cannot receive " + qtyToReceive + " units. Only " +
                        remaining + " units outstanding for " + item.getProduct().getName());
            }

            item.setQuantityReceived(item.getQuantityReceived() + qtyToReceive);
            purchaseOrderItemRepository.save(item);

            // Update inventory and record traceable stock movement
            Long locId = receipt.getLocationId() != null ? receipt.getLocationId() :
                    (po.getTargetLocation() != null ? po.getTargetLocation().getId() : null);

            inventoryService.recordStockMovement(
                    item.getProduct().getId(),
                    locId,
                    MovementType.PO_RECEIPT,
                    qtyToReceive,
                    "Received against PO " + po.getOrderNumber(),
                    po.getOrderNumber(),
                    receivedBy
            );
        }

        // Determine if fully received or partially received
        boolean allComplete = po.getItems().stream().allMatch(PurchaseOrderItem::isFullyReceived);
        boolean anyReceived = po.getItems().stream().anyMatch(i -> i.getQuantityReceived() > 0);

        if (allComplete) {
            po.setStatus(OrderStatus.RECEIVED);
        } else if (anyReceived) {
            po.setStatus(OrderStatus.PARTIALLY_RECEIVED);
        }

        po.setUpdatedAt(LocalDateTime.now());
        return purchaseOrderRepository.save(po);
    }

    @Transactional
    public PurchaseOrder cancelOrder(Long id) {
        PurchaseOrder po = getOrderById(id);
        if (po.getStatus() == OrderStatus.RECEIVED || po.getStatus() == OrderStatus.PARTIALLY_RECEIVED) {
            throw new IllegalStateException("Cannot cancel an order that has already been received");
        }
        po.setStatus(OrderStatus.CANCELLED);
        po.setUpdatedAt(LocalDateTime.now());
        return purchaseOrderRepository.save(po);
    }
}
