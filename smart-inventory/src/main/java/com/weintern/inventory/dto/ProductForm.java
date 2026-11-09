package com.weintern.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class ProductForm {

    @NotBlank(message = "Enter a SKU.")
    @Size(max = 40, message = "SKU can be at most 40 characters.")
    @Pattern(regexp = "^[A-Za-z0-9._-]*$", message = "SKU can only contain letters, numbers, dot, dash and underscore.")
    private String sku;

    @NotBlank(message = "Enter a product name.")
    @Size(max = 150, message = "Name can be at most 150 characters.")
    private String name;

    @Size(max = 500, message = "Description can be at most 500 characters.")
    private String description;

    @NotNull(message = "Choose a category.")
    private Long categoryId;

    @NotBlank(message = "Enter a unit, for example pcs or box.")
    @Size(max = 20, message = "Unit can be at most 20 characters.")
    private String unit = "pcs";

    @NotNull(message = "Enter the cost price.")
    @DecimalMin(value = "0.00", message = "Cost price cannot be negative.")
    @Digits(integer = 10, fraction = 2, message = "Use at most 2 decimal places.")
    private BigDecimal costPrice;

    @NotNull(message = "Enter the selling price.")
    @DecimalMin(value = "0.00", message = "Selling price cannot be negative.")
    @Digits(integer = 10, fraction = 2, message = "Use at most 2 decimal places.")
    private BigDecimal sellingPrice;

    @NotNull(message = "Enter a reorder level.")
    @Min(value = 0, message = "Reorder level cannot be negative.")
    @Max(value = 1_000_000, message = "Reorder level is too large.")
    private Integer reorderLevel = 10;

    /** Only used when creating a product. */
    @Min(value = 0, message = "Opening stock cannot be negative.")
    @Max(value = 1_000_000, message = "Opening stock is too large.")
    private Integer openingStock = 0;

    private Long locationId;
}
