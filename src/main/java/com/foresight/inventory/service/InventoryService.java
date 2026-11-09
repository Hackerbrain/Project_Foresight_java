package com.foresight.inventory.service;

import com.foresight.inventory.dto.StockAdjustmentRequest;
import com.foresight.inventory.dto.StockTransferRequest;
import com.foresight.inventory.entity.*;
import com.foresight.inventory.repository.ProductRepository;
import com.foresight.inventory.repository.StockLocationRepository;
import com.foresight.inventory.repository.StockMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final StockLocationRepository stockLocationRepository;
    private final StockMovementRepository stockMovementRepository;

    public InventoryService(ProductRepository productRepository,
                            StockLocationRepository stockLocationRepository,
                            StockMovementRepository stockMovementRepository) {
        this.productRepository = productRepository;
        this.stockLocationRepository = stockLocationRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional
    public StockMovement recordStockMovement(Long productId, Long locationId, MovementType type,
                                             int quantityChange, String reason, String referenceNumber,
                                             String performedBy) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));

        StockLocation location = null;
        if (locationId != null) {
            location = stockLocationRepository.findById(locationId).orElse(null);
        } else {
            location = product.getDefaultLocation();
        }

        int previousStock = product.getCurrentStock() != null ? product.getCurrentStock() : 0;
        int newStock = previousStock + quantityChange;

        if (newStock < 0) {
            throw new IllegalArgumentException("Cannot reduce stock below zero. Current stock: " +
                    previousStock + ", requested change: " + quantityChange);
        }

        product.setCurrentStock(newStock);
        productRepository.save(product);

        StockMovement movement = new StockMovement(
                product,
                location,
                null,
                type,
                quantityChange,
                previousStock,
                newStock,
                reason != null ? reason : type.name(),
                referenceNumber,
                performedBy != null ? performedBy : "System"
        );

        return stockMovementRepository.save(movement);
    }

    @Transactional
    public StockMovement handleAdjustment(StockAdjustmentRequest request, String performedBy) {
        int delta = request.getQuantityChange();
        if (request.getType() == MovementType.STOCK_OUT && delta > 0) {
            delta = -delta;
        }

        return recordStockMovement(
                request.getProductId(),
                request.getLocationId(),
                request.getType(),
                delta,
                request.getReason(),
                request.getReferenceNumber() != null ? request.getReferenceNumber() : "ADJ-" + System.currentTimeMillis(),
                performedBy
        );
    }

    @Transactional
    public StockMovement transferStock(StockTransferRequest request, String performedBy) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + request.getProductId()));

        StockLocation fromLoc = stockLocationRepository.findById(request.getFromLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Origin location not found: " + request.getFromLocationId()));

        StockLocation toLoc = stockLocationRepository.findById(request.getToLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Destination location not found: " + request.getToLocationId()));

        if (fromLoc.getId().equals(toLoc.getId())) {
            throw new IllegalArgumentException("Origin and destination locations must be different");
        }

        int current = product.getCurrentStock() != null ? product.getCurrentStock() : 0;
        if (current < request.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock to transfer. Available: " + current + ", requested: " + request.getQuantity());
        }

        // Create traceable transfer movement
        StockMovement transferMovement = new StockMovement(
                product,
                fromLoc,
                toLoc,
                MovementType.TRANSFER,
                request.getQuantity(),
                current,
                current, // overall global total remains unchanged, but transfer is logged with location audit
                request.getReason() != null ? request.getReason() : "Inter-warehouse stock transfer",
                "TR-" + System.currentTimeMillis(),
                performedBy != null ? performedBy : "System"
        );

        return stockMovementRepository.save(transferMovement);
    }

    @Transactional(readOnly = true)
    public List<StockMovement> getRecentMovements() {
        return stockMovementRepository.findTop50ByOrderByTimestampDesc();
    }

    @Transactional(readOnly = true)
    public Page<StockMovement> getRecentMovementsPage(Pageable pageable) {
        return stockMovementRepository.findAllByOrderByTimestampDesc(pageable);
    }

    @Transactional(readOnly = true)
    public List<StockMovement> getProductMovements(Long productId) {
        return stockMovementRepository.findByProductIdOrderByTimestampDesc(productId);
    }
}
