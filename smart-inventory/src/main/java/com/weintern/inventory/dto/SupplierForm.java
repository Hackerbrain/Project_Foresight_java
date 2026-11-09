package com.weintern.inventory.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class SupplierForm {

    @NotBlank(message = "Enter the supplier name.")
    @Size(max = 150, message = "Name can be at most 150 characters.")
    private String name;

    @Size(max = 100, message = "Contact person can be at most 100 characters.")
    private String contactPerson;

    @Email(message = "Enter a valid email address.")
    @Size(max = 120, message = "Email can be at most 120 characters.")
    private String email;

    @Pattern(regexp = "^[0-9+()\\-\\s]{0,30}$", message = "Phone can contain digits, spaces, + ( ) and -.")
    private String phone;

    @Size(max = 255, message = "Address can be at most 255 characters.")
    private String address;

    private boolean active = true;

    private List<Long> productIds = new ArrayList<>();
}
