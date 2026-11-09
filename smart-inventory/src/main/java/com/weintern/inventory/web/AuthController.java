package com.weintern.inventory.web;

import com.weintern.inventory.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final DashboardService dashboardService;

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardService.load());
        return "dashboard";
    }
}
