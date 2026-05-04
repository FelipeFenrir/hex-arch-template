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
        String tenantId = TenantContextHolder.currentTenantOrNull();
        if (tenantId != null) {
            model.addAttribute("currentTenant", tenantId);
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
}
