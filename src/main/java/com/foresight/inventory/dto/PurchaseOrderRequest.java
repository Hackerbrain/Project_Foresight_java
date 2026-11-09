package com.foresight.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class PurchaseOrderRequest {

    @NotNull(message = "Supplier is required")
    private Long supplierId;

    private Long targetLocationId;

    private String notes;

    @NotEmpty(message = "Order must have at least one line item")
    @Valid
    private List<PurchaseOrderItemRequest> items;

    public PurchaseOrderRequest() {}

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

    public Long getTargetLocationId() { return targetLocationId; }
    public void setTargetLocationId(Long targetLocationId) { this.targetLocationId = targetLocationId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<PurchaseOrderItemRequest> getItems() { return items; }
    public void setItems(List<PurchaseOrderItemRequest> items) { this.items = items; }
}
