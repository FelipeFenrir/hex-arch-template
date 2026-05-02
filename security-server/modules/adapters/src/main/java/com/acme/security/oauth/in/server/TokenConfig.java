package com.acme.security.oauth.in.server;

import com.acme.shared.TenantContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration
public class TokenConfig {

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return (context) -> {
            // Adiciona o tenant_id nas claims do Access Token e ID Token
            String tenantId = TenantContextHolder.currentTenantOrNull();
            if (tenantId != null) {
                context.getClaims().claim("tenant_id", tenantId);
            }

            // Você também pode injetar as Roles do usuário aqui se desejar
            var authorities = context.getPrincipal().getAuthorities();
            context.getClaims().claim("roles", authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList());
        };
    }
}
