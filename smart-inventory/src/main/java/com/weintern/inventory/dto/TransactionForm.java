package com.weintern.inventory.dto;

import com.weintern.inventory.domain.MovementType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TransactionForm {

    @NotNull(message = "Choose a product.")
    private Long productId;

    @NotNull(message = "Choose a transaction type.")
    private MovementType type = MovementType.STOCK_IN;

    /** Units moved for stock in/out; the counted quantity for an adjustment. */
    @NotNull(message = "Enter a quantity.")
    @Min(value = 0, message = "Quantity cannot be negative.")
    @Max(value = 1_000_000, message = "Quantity is too large.")
    private Integer quantity;

    @NotBlank(message = "Enter a reason so the change can be audited.")
    @Size(max = 255, message = "Reason can be at most 255 characters.")
    private String reason;

    @Size(max = 60, message = "Reference can be at most 60 characters.")
    private String reference;
}
