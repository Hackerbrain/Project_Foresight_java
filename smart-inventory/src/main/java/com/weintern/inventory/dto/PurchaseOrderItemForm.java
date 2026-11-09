package com.weintern.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class PurchaseOrderItemForm {

    @NotNull(message = "Choose a product.")
    private Long productId;

    @NotNull(message = "Enter a quantity.")
    @Min(value = 1, message = "Quantity must be at least 1.")
    @Max(value = 1_000_000, message = "Quantity is too large.")
    private Integer quantity;

    /** Optional: defaults to the product's cost price. */
    @DecimalMin(value = "0.00", message = "Unit cost cannot be negative.")
    @Digits(integer = 10, fraction = 2, message = "Use at most 2 decimal places.")
    private BigDecimal unitCost;
}
