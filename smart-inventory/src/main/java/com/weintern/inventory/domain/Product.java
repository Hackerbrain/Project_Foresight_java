package com.weintern.inventory.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "products", indexes = @Index(name = "idx_product_name", columnList = "name"))
@Getter @Setter @NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String sku;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(optional = false)
    private Category category;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(nullable = false)
    private int reorderLevel;

    @Column(nullable = false)
    private int quantityOnHand;

    @ManyToOne
    private StockLocation location;

    @Column(nullable = false)
    private boolean active = true;

    /** Optimistic lock: protects against two users saving the same product at once. */
    @Version
    private Long version;

    /** Low stock when on-hand quantity has fallen to, or below, the reorder level. */
    public boolean isLowStock() {
        return quantityOnHand <= reorderLevel;
    }

    // --- presentation helpers for the stock gauge (not persisted) ---
    private int gaugeScale() {
        return Math.max(1, Math.max(quantityOnHand, reorderLevel * 2));
    }

    public int getGaugePercent() {
        return Math.min(100, quantityOnHand * 100 / gaugeScale());
    }

    public int getReorderMarkPercent() {
        return Math.min(100, reorderLevel * 100 / gaugeScale());
    }
}
