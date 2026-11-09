package com.weintern.inventory.service;

import com.weintern.inventory.domain.AppUser;
import com.weintern.inventory.dto.UserForm;
import com.weintern.inventory.exception.BusinessException;
import com.weintern.inventory.exception.ResourceNotFoundException;
import com.weintern.inventory.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserService implements UserDetailsService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser u = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user"));
        return User.withUsername(u.getUsername())
                .password(u.getPasswordHash())
                .roles(u.getRole().name())
                .disabled(!u.isEnabled())
                .build();
    }

    @Transactional(readOnly = true)
    public List<AppUser> listAll() { return userRepository.findAll(); }

    @Transactional
    public AppUser create(UserForm form) {
        String username = form.getUsername().trim();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new BusinessException("Username \"" + username + "\" is already taken.");
        }
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setFullName(form.getFullName().trim());
        u.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        u.setRole(form.getRole());
        return userRepository.save(u);
    }

    @Transactional
    public AppUser setEnabled(Long id, boolean enabled, String actingUsername) {
        AppUser u = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
        if (!enabled && u.getUsername().equalsIgnoreCase(actingUsername)) {
            throw new BusinessException("You cannot disable your own account.");
        }
        u.setEnabled(enabled);
        return u;
    }
}
