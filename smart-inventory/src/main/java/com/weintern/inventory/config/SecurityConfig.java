package com.weintern.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Server-side authorization. Anyone signed in can read; changes are limited by role:
 * <ul>
 *   <li>ADMIN - everything, including user management</li>
 *   <li>INVENTORY_STAFF - products, categories, stock transactions, receiving orders</li>
 *   <li>PURCHASING_STAFF - suppliers, purchase orders, receiving orders</li>
 * </ul>
 * CSRF protection stays enabled (the default); every form carries a token.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String INVENTORY = "INVENTORY_STAFF";
    private static final String PURCHASING = "PURCHASING_STAFF";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/error").permitAll()
                .requestMatchers("/users/**").hasRole(ADMIN)

                .requestMatchers(HttpMethod.POST, "/purchase-orders/*/receive").hasAnyRole(ADMIN, INVENTORY, PURCHASING)
                .requestMatchers("/purchase-orders/new").hasAnyRole(ADMIN, PURCHASING)
                .requestMatchers(HttpMethod.POST, "/purchase-orders/**").hasAnyRole(ADMIN, PURCHASING)

                .requestMatchers("/inventory/transaction").hasAnyRole(ADMIN, INVENTORY)

                .requestMatchers("/products/new", "/products/*/edit").hasAnyRole(ADMIN, INVENTORY)
                .requestMatchers(HttpMethod.POST, "/products/**", "/categories/**").hasAnyRole(ADMIN, INVENTORY)

                .requestMatchers("/suppliers/new", "/suppliers/*/edit").hasAnyRole(ADMIN, PURCHASING)
                .requestMatchers(HttpMethod.POST, "/suppliers/**").hasAnyRole(ADMIN, PURCHASING)

                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
