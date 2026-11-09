package com.weintern.inventory.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_items")
@Getter @Setter @NoArgsConstructor
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(optional = false)
    private Product product;

    @Column(nullable = false)
    private int quantityOrdered;

    @Column(nullable = false)
    private int quantityReceived;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitCost;

    public int getOutstanding() { return quantityOrdered - quantityReceived; }

    public BigDecimal getLineTotal() {
        return unitCost.multiply(BigDecimal.valueOf(quantityOrdered));
    }
}
