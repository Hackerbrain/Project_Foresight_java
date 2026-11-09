package com.weintern.inventory.web;

import com.weintern.inventory.domain.Role;
import com.weintern.inventory.dto.UserForm;
import com.weintern.inventory.exception.BusinessException;
import com.weintern.inventory.service.AppUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final AppUserService userService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.listAll());
        return "users/list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("form", new UserForm());
        model.addAttribute("roles", Role.values());
        return "users/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") UserForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                userService.create(form);
                redirect.addFlashAttribute("success", "User " + form.getUsername() + " created.");
                return "redirect:/users";
            } catch (BusinessException ex) {
                model.addAttribute("error", ex.getMessage());
            }
        }
        model.addAttribute("roles", Role.values());
        return "users/form";
    }

    @PostMapping("/{id}/enabled")
    public String setEnabled(@PathVariable Long id, @RequestParam boolean enabled,
                             Principal principal, RedirectAttributes redirect) {
        try {
            userService.setEnabled(id, enabled, principal.getName());
            redirect.addFlashAttribute("success", enabled ? "User enabled." : "User disabled.");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/users";
    }
}
