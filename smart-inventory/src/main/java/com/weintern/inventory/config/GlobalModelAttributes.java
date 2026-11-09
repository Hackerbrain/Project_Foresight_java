package com.weintern.inventory.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final String currency;

    public GlobalModelAttributes(@Value("${app.currency-symbol:₹}") String currency) {
        this.currency = currency;
    }

    @ModelAttribute("currency")
    public String currency() { return currency; }
}
