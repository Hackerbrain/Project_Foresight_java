package com.foresight.inventory.dto;

import com.foresight.inventory.entity.MovementType;
import jakarta.validation.constraints.NotNull;

public class StockAdjustmentRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    private Long locationId;

    @NotNull(message = "Movement type is required")
    private MovementType type; // STOCK_IN, STOCK_OUT, ADJUSTMENT

    @NotNull(message = "Quantity change is required")
    private Integer quantityChange;

    private String reason;

    private String referenceNumber;

    public StockAdjustmentRequest() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }

    public MovementType getType() { return type; }
    public void setType(MovementType type) { this.type = type; }

    public Integer getQuantityChange() { return quantityChange; }
    public void setQuantityChange(Integer quantityChange) { this.quantityChange = quantityChange; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
}
