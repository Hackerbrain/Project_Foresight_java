package com.foresight.inventory.controller;

import com.foresight.inventory.dto.StockAdjustmentRequest;
import com.foresight.inventory.dto.StockTransferRequest;
import com.foresight.inventory.entity.StockMovement;
import com.foresight.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/adjustment")
    public ResponseEntity<?> adjustStock(@Valid @RequestBody StockAdjustmentRequest request, Principal principal) {
        String user = principal != null ? principal.getName() : "Anonymous";
        try {
            StockMovement movement = inventoryService.handleAdjustment(request, user);
            return ResponseEntity.ok(movement);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferStock(@Valid @RequestBody StockTransferRequest request, Principal principal) {
        String user = principal != null ? principal.getName() : "Anonymous";
        try {
            StockMovement movement = inventoryService.transferStock(request, user);
            return ResponseEntity.ok(movement);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/movements")
    public ResponseEntity<List<StockMovement>> getMovements() {
        return ResponseEntity.ok(inventoryService.getRecentMovements());
    }

    @GetMapping("/movements/product/{productId}")
    public ResponseEntity<List<StockMovement>> getProductMovements(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getProductMovements(productId));
    }
}
