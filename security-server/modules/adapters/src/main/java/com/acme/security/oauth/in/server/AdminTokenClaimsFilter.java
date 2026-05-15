package com.acme.security.oauth.in.server;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Filtro de validação de token para APIs de administração de tenants.
 * Não é um @Component - é criado como @Bean em SecurityConfig para evitar circular dependency.
 */
public class AdminTokenClaimsFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;

    public AdminTokenClaimsFilter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = Objects.requireNonNull(jwtDecoder, "jwtDecoder must not be null");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri == null || !uri.startsWith("/api/v1/tenants");
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Bearer token is required for tenant administration APIs.");
            return;
        }

        String token = authorization.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Bearer token is required for tenant administration APIs.");
            return;
        }

        try {
            Jwt jwt = jwtDecoder.decode(token);
            if (!hasAdminRole(jwt)) {
                writeError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Admin or super-admin role is required for tenant administration APIs.");
                return;
            }

            List<GrantedAuthority> authorities = toAuthorities(jwt);
            var authentication = new UsernamePasswordAuthenticationToken(jwt.getSubject(), token, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException exception) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired bearer token.");
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private static boolean hasAdminRole(Jwt jwt) {
        return containsRole(jwt.getClaimAsStringList("roles"), "ROLE_ADMIN")
                || containsRole(jwt.getClaimAsStringList("roles"), "ROLE_SUPER_ADMIN")
                || containsRole(jwt.getClaimAsStringList("authorities"), "ROLE_ADMIN")
                || containsRole(jwt.getClaimAsStringList("authorities"), "ROLE_SUPER_ADMIN");
    }

    private static boolean containsRole(Collection<String> values, String role) {
        if (values == null || values.isEmpty()) {
            return false;
        }
        return values.stream().anyMatch(role::equals);
    }

    private static List<GrantedAuthority> toAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        addRoles(authorities, jwt.getClaimAsStringList("roles"));
        addRoles(authorities, jwt.getClaimAsStringList("authorities"));
        return authorities;
    }

    private static void addRoles(List<GrantedAuthority> authorities, List<String> roles) {
        if (roles == null) {
            return;
        }

        roles.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
    }

    private static void writeError(HttpServletResponse response, int status, String detail) throws IOException {
        response.setStatus(status);
        response.setContentType("application/problem+json");
        response.getWriter().write("""
                {
                  "type": "about:blank",
                  "title": "Authorization error",
                  "status": %d,
                  "detail": "%s"
                }
                """.formatted(status, detail));
    }
}


