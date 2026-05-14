package com.acme.security.oauth.in.web;

import com.acme.security.user.dto.command.UserRegistrationCommand;
import com.acme.security.user.port.in.usecase.RegisterUserUseCase;
import com.acme.security.tenant.erros.TenantDomainErrors;
import com.acme.shared.TenantContextHolder;
import com.acme.shared.pattern.result.DomainError;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Controller
public class UserRegistrationController {

    private final RegisterUserUseCase registerUserUseCase;

    public UserRegistrationController(RegisterUserUseCase registerUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
    }

    @PostMapping("/register")
    public String register(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("email") String email,
                                       Model model) {

        // O tenantId vem do contexto (capturado pelo filtro de subdomínio)
        var tenantResult = TenantContextHolder.currentTenantRequired(TenantDomainErrors::tenantContextMissing);
        if (tenantResult.isFailure()) {
            model.addAttribute("error", tenantResult.fold(ignored -> null, DomainError::message));
            return "register";
        }

        String tenantId = tenantResult.fold(value -> value, ignored -> null);

        var command = new UserRegistrationCommand(
                username,
                password,
                email,
                tenantId,
                Set.of("ROLE_USER")
        );

        var result = registerUserUseCase.execute(command);

        if (result.isSuccess()) {
            return "redirect:/login?registered=true";
        } else {
            model.addAttribute("error", result.fold(ignored -> null, DomainError::message));
            return "register";
        }
    }
}
