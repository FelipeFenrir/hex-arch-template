package com.acme.security.user.in.dto.request;

import com.acme.security.user.dto.command.UpdateUserCommand;

import java.util.Set;

public record UpdateUserRequest(
        String username,
        String password,
        Set<String> roles,
        Boolean active
) {
    public UpdateUserCommand toCommand(String id, String tenantId) {
        return new UpdateUserCommand(
                id,
                tenantId,
                username,
                password,
                roles,
                active
        );
    }
}

