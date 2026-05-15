package com.acme.security.tenant.dto.command;

public record UpdateTenantCommand(
        String id,
        String name,
        String slug,
        Boolean active
) {
}

