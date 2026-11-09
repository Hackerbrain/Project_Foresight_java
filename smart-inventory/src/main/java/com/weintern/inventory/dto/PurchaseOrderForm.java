package com.weintern.inventory.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter @Setter
public class PurchaseOrderForm {

    @NotNull(message = "Choose a supplier.")
    private Long supplierId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @FutureOrPresent(message = "Expected date cannot be in the past.")
    private LocalDate expectedDate;

    @Size(max = 500, message = "Notes can be at most 500 characters.")
    private String notes;
}
