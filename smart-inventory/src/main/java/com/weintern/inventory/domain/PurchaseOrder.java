package com.weintern.inventory.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
@Getter @Setter @NoArgsConstructor
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String orderNumber;

    @ManyToOne(optional = false)
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PoStatus status = PoStatus.DRAFT;

    private LocalDate expectedDate;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, length = 50)
    private String createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime orderedAt;
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<PurchaseOrderItem> items = new ArrayList<>();

    @Version
    private Long version;

    @PrePersist
    void onCreate() { createdAt = LocalDateTime.now(); }

    public void addItem(PurchaseOrderItem item) {
        item.setPurchaseOrder(this);
        items.add(item);
    }

    public BigDecimal getTotalAmount() {
        return items.stream().map(PurchaseOrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalOrdered() {
        return items.stream().mapToInt(PurchaseOrderItem::getQuantityOrdered).sum();
    }

    public int getTotalReceived() {
        return items.stream().mapToInt(PurchaseOrderItem::getQuantityReceived).sum();
    }

    public int getTotalOutstanding() { return getTotalOrdered() - getTotalReceived(); }
}
