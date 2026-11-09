package com.weintern.inventory.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Ledger row: every change to a product's quantity creates exactly one of these. */
@Entity
@Table(name = "stock_movements", indexes = {
        @Index(name = "idx_movement_created", columnList = "createdAt"),
        @Index(name = "idx_movement_product", columnList = "product_id")})
@Getter @Setter @NoArgsConstructor
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovementType type;

    /** Signed change: positive adds stock, negative removes it. */
    @Column(nullable = false)
    private int quantityChange;

    @Column(nullable = false)
    private int balanceAfter;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(length = 60)
    private String reference;

    @Column(nullable = false, length = 50)
    private String performedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }
}
