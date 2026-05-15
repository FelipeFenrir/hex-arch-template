package com.acme.security.tenant.dto.command;

public record CreateTenantCommand(
        String id,
        String name,
        String slug
) {
}

