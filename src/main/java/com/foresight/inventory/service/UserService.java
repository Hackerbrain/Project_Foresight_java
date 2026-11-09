package com.foresight.inventory.service;

import com.foresight.inventory.entity.Role;
import com.foresight.inventory.entity.User;
import com.foresight.inventory.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User registerUser(String email, String rawPassword, String fullName, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists.");
        }
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(email, encodedPassword, fullName, role != null ? role : Role.INVENTORY_STAFF, "LOCAL");
        return userRepository.save(user);
    }

    @Transactional
    public User processOAuthPostLogin(String email, String name) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (!"GOOGLE".equalsIgnoreCase(user.getAuthProvider())) {
                user.setAuthProvider("GOOGLE");
                user = userRepository.save(user);
            }
            return user;
        }

        // Auto-provision user logging in via Google
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFullName(name != null ? name : "Google User");
        newUser.setRole(Role.INVENTORY_STAFF);
        newUser.setAuthProvider("GOOGLE");
        newUser.setEnabled(true);
        return userRepository.save(newUser);
    }
}
