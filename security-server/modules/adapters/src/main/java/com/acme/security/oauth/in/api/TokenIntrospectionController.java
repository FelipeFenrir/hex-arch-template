package com.acme.security.oauth.in.api;

import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TokenIntrospectionController {

    private final OAuth2AuthorizationService authorizationService;

    public TokenIntrospectionController(OAuth2AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @PostMapping("/oauth2/introspect")
    public Map<String, Object> introspect(@RequestParam("token") String token) {
        OAuth2Authorization authorization = this.authorizationService.findByToken(
                token, OAuth2TokenType.ACCESS_TOKEN);

        Map<String, Object> claims = new HashMap<>();

        if (authorization == null) {
            claims.put("active", false);
            return claims;
        }

        claims.put("active", true);
        claims.put("sub", authorization.getPrincipalName());
        claims.put("scope", String.join(" ", authorization.getAuthorizedScopes()));
        // Injetando o tenant_id na resposta da introspeção também
        claims.put("tenant_id", authorization.getAttribute("tenant_id"));

        return claims;
    }
}
