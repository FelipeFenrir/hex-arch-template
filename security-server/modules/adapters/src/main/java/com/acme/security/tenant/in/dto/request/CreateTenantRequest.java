package com.acme.security.tenant.in.dto.request;

import com.acme.security.tenant.dto.command.CreateTenantCommand;

public record CreateTenantRequest(
        String id,
        String name,
        String slug
) {
    public CreateTenantCommand toCommand() {
        return new CreateTenantCommand(id, name, slug);
    }
}


