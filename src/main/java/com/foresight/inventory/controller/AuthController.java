package com.foresight.inventory.controller;

import com.foresight.inventory.entity.Role;
import com.foresight.inventory.entity.User;
import com.foresight.inventory.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        String fullName = request.get("fullName");
        String roleStr = request.getOrDefault("role", "INVENTORY_STAFF");

        try {
            Role role = Role.valueOf(roleStr);
            User user = userService.registerUser(email, password, fullName, role);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "User registered successfully");
            resp.put("email", user.getEmail());
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication,
                                            @AuthenticationPrincipal Object principal) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            Map<String, Object> anonymous = new HashMap<>();
            anonymous.put("authenticated", false);
            return ResponseEntity.ok(anonymous);
        }

        String email = authentication.getName();
        String name = email;
        String role = "INVENTORY_STAFF";

        if (principal instanceof OAuth2User oauthUser) {
            email = oauthUser.getAttribute("email");
            name = oauthUser.getAttribute("name");
            User user = userService.processOAuthPostLogin(email, name);
            role = user.getRole().name();
        } else {
            var userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                name = userOpt.get().getFullName();
                role = userOpt.get().getRole().name();
            }
        }

        Map<String, Object> details = new HashMap<>();
        details.put("authenticated", true);
        details.put("email", email);
        details.put("name", name);
        details.put("role", role);
        return ResponseEntity.ok(details);
    }
}
