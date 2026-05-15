package com.acme.security.user.in.dto.request;

import com.acme.security.user.dto.command.UserRegistrationCommand;

import java.util.Set;

public record CreateUserRequest(
        String username,
        String password,
        String email,
        Set<String> roles
) {
    public UserRegistrationCommand toCommand(String tenantId) {
        Set<String> resolvedRoles = (roles == null || roles.isEmpty()) ? Set.of("ROLE_USER") : roles;

        return new UserRegistrationCommand(
                username,
                password,
                email,
                tenantId,
                resolvedRoles
        );
    }
}

