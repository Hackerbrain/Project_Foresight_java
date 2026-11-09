package com.foresight.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ReceiveItemRequest {

    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotNull(message = "Quantity to receive is required")
    @Min(value = 1, message = "Quantity received must be at least 1")
    private Integer quantityReceived;

    private Long locationId;

    public ReceiveItemRequest() {}

    public ReceiveItemRequest(Long itemId, Integer quantityReceived, Long locationId) {
        this.itemId = itemId;
        this.quantityReceived = quantityReceived;
        this.locationId = locationId;
    }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public Integer getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; }

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
}
