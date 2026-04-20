package com.acme.security.oauth.in.web;

import com.acme.shared.TenantContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage(Model model) {
        // Opcional: Adicionar o ID do tenant para debug visual no HTML
        if (TenantContextHolder.currentTenant() != null) {
            model.addAttribute("currentTenant", TenantContextHolder.currentTenant());
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
}
