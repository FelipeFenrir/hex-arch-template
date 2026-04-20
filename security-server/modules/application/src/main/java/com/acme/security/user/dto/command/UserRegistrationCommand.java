package com.acme.security.user.dto.command;

import java.util.Set;

public record UserRegistrationCommand(
        String username,
        String password,
        String email,
        String tenantId,
        Set<String> roles
) { }
