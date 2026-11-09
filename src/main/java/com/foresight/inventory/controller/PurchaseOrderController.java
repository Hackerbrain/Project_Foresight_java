package com.foresight.inventory.controller;

import com.foresight.inventory.dto.PurchaseOrderRequest;
import com.foresight.inventory.dto.ReceiveItemRequest;
import com.foresight.inventory.entity.PurchaseOrder;
import com.foresight.inventory.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrder>> getAllOrders() {
        return ResponseEntity.ok(purchaseOrderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrder> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getOrderById(id));
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody PurchaseOrderRequest request, Principal principal) {
        String user = principal != null ? principal.getName() : "Anonymous";
        try {
            PurchaseOrder order = purchaseOrderService.createOrder(request, user);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<?> markAsOrdered(@PathVariable Long id) {
        try {
            PurchaseOrder order = purchaseOrderService.markAsOrdered(id);
            return ResponseEntity.ok(order);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<?> receiveItems(@PathVariable Long id,
                                          @RequestBody List<ReceiveItemRequest> receipts,
                                          Principal principal) {
        String user = principal != null ? principal.getName() : "Anonymous";
        try {
            PurchaseOrder order = purchaseOrderService.receiveItems(id, receipts, user);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            PurchaseOrder order = purchaseOrderService.cancelOrder(id);
            return ResponseEntity.ok(order);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
