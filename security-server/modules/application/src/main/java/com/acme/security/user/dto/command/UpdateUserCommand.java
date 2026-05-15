package com.acme.security.user.dto.command;

import java.util.Set;

public record UpdateUserCommand(
        String id,
        String tenantId,
        String username,
        String password,
        Set<String> roles,
        Boolean active
) {
}

