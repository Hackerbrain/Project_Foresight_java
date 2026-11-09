package com.weintern.inventory.dto;

import com.weintern.inventory.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserForm {

    @NotBlank(message = "Enter a username.")
    @Pattern(regexp = "^[A-Za-z0-9._-]{3,30}$", message = "Username must be 3-30 characters: letters, numbers, dot, dash, underscore.")
    private String username;

    @NotBlank(message = "Enter the user's full name.")
    @Size(max = 100, message = "Name can be at most 100 characters.")
    private String fullName;

    @NotBlank(message = "Enter a password.")
    @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters.")
    private String password;

    @NotNull(message = "Choose a role.")
    private Role role = Role.INVENTORY_STAFF;
}
