package com.acme.security.oauth.in.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Set;

@Controller
public class AuthorizationConsentController {

    @GetMapping("/oauth2/consent")
    public String consent(
            Principal principal,
            Model model,
            @RequestParam("client_id") String clientId, // Nomeado explicitamente
            @RequestParam("scope") String scope,       // Nomeado explicitamente
            @RequestParam("state") String state,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri) {

        // Aqui preparamos os dados para a futura tela de consentimento
        model.addAttribute("clientId", clientId);
        model.addAttribute("state", state);
        model.addAttribute("scopes", Set.of(scope.split(" ")));
        model.addAttribute("redirectUri", redirectUri);
        model.addAttribute("principalName", principal.getName());

        return "consent"; // Nome do template HTML que faremos depois
    }
}
