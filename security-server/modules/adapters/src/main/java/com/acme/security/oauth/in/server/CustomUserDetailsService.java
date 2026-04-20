package com.acme.security.oauth.in.server;

import com.acme.security.user.port.in.usecase.AuthenticateUserUseCase;
import com.acme.security.user.User;
import com.acme.shared.TenantContextHolder;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.vo.TenantId;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthenticateUserUseCase authenticateUserUseCase;

    public CustomUserDetailsService(AuthenticateUserUseCase authenticateUserUseCase) {
        this.authenticateUserUseCase = Objects.requireNonNull(authenticateUserUseCase,
                "authenticateUserUseCase must not be null");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Recuperamos o tenant identificado pelo nosso filtro de subdomínio
        TenantId tenantId = TenantId.fromString(TenantContextHolder.currentTenant());

        Result<User, DomainError> result = authenticateUserUseCase.loadUserByUsername(username, tenantId);

        if (result.isFailure()) {
            result.onFailure(error -> {
                throw new UsernameNotFoundException(error.message());
            });
        }

        return result
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.username())
                        .password(user.password()) // Já está em Argon2
                        .disabled(!user.isActive())
                        .authorities(user.roles().toArray(new String[0]))
                        .build())
                .getOrElseThrow(error -> new RuntimeException());
    }
}
