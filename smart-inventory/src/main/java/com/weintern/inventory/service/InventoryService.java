package com.weintern.inventory.service;

import com.weintern.inventory.domain.MovementType;
import com.weintern.inventory.domain.Product;
import com.weintern.inventory.domain.StockMovement;
import com.weintern.inventory.exception.BusinessException;
import com.weintern.inventory.exception.ResourceNotFoundException;
import com.weintern.inventory.repository.ProductRepository;
import com.weintern.inventory.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The single place where a product's quantity is allowed to change.
 * Every change locks the product row, validates the result and writes a ledger entry
 * in the same transaction, so inventory and history can never disagree.
 */
@Service
@RequiredArgsConstructor
public class InventoryService {

    public static final int MAX_QUANTITY = 1_000_000;
    public static final int PAGE_SIZE = 15;

    private final ProductRepository productRepository;
    private final StockMovementRepository movementRepository;

    @Transactional
    public StockMovement record(MovementType type, Long productId, int quantity, String reason, String reference) {
        return switch (type) {
            case STOCK_IN -> stockIn(productId, quantity, reason, reference);
            case STOCK_OUT -> stockOut(productId, quantity, reason, reference);
            case ADJUSTMENT -> adjust(productId, quantity, reason);
            default -> throw new BusinessException("Purchase-order receipts are recorded from the purchase order.");
        };
    }

    @Transactional
    public StockMovement stockIn(Long productId, int quantity, String reason, String reference) {
        requirePositive(quantity);
        Product product = lockActive(productId);
        return apply(product, MovementType.STOCK_IN, quantity, reason, reference);
    }

    @Transactional
    public StockMovement stockOut(Long productId, int quantity, String reason, String reference) {
        requirePositive(quantity);
        Product product = lockActive(productId);
        return apply(product, MovementType.STOCK_OUT, -quantity, reason, reference);
    }

    /** Sets stock to a counted quantity and records the difference. */
    @Transactional
    public StockMovement adjust(Long productId, int countedQuantity, String reason) {
        if (countedQuantity < 0 || countedQuantity > MAX_QUANTITY) {
            throw new BusinessException("Counted quantity must be between 0 and " + MAX_QUANTITY + ".");
        }
        Product product = lockActive(productId);
        int delta = countedQuantity - product.getQuantityOnHand();
        if (delta == 0) {
            throw new BusinessException("Counted quantity matches the current stock, so there is nothing to adjust.");
        }
        return apply(product, MovementType.ADJUSTMENT, delta, reason, null);
    }

    /** Used by purchase-order receiving. Runs inside the caller's transaction. */
    @Transactional
    public StockMovement receiveStock(Long productId, int quantity, String reason, String reference) {
        requirePositive(quantity);
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        return apply(product, MovementType.PO_RECEIPT, quantity, reason, reference);
    }

    @Transactional(readOnly = true)
    public Page<StockMovement> searchLedger(String q, MovementType type, int page) {
        Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
        return movementRepository.search(q == null ? "" : q.trim(), type == null,
                type == null ? MovementType.STOCK_IN : type, PageRequest.of(Math.max(page, 0), PAGE_SIZE, sort));
    }

    @Transactional(readOnly = true)
    public List<StockMovement> recentForProduct(Long productId) {
        return movementRepository.findTop20ByProductIdOrderByCreatedAtDescIdDesc(productId);
    }

    // ------------------------------------------------------------------

    private StockMovement apply(Product product, MovementType type, int delta, String reason, String reference) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("A reason is required for every stock change.");
        }
        long newBalance = (long) product.getQuantityOnHand() + delta;
        if (newBalance < 0) {
            throw new BusinessException("Not enough stock of " + product.getSku() + ": " + product.getQuantityOnHand()
                    + " on hand, " + (-delta) + " requested.");
        }
        if (newBalance > MAX_QUANTITY) {
            throw new BusinessException("Stock of " + product.getSku() + " cannot exceed " + MAX_QUANTITY + " units.");
        }
        product.setQuantityOnHand((int) newBalance);

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setType(type);
        movement.setQuantityChange(delta);
        movement.setBalanceAfter((int) newBalance);
        movement.setReason(reason.trim());
        movement.setReference(reference == null || reference.isBlank() ? null : reference.trim());
        movement.setPerformedBy(currentUsername());
        return movementRepository.save(movement);
    }

    private Product lockActive(Long productId) {
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        if (!product.isActive()) {
            throw new BusinessException("Product " + product.getSku() + " is archived and cannot be changed.");
        }
        return product;
    }

    private void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("Quantity must be greater than zero.");
        }
        if (quantity > MAX_QUANTITY) {
            throw new BusinessException("Quantity cannot exceed " + MAX_QUANTITY + ".");
        }
    }

    static String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "system";
        }
        return auth.getName();
    }
}
