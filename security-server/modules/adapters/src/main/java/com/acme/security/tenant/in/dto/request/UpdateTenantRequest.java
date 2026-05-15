package com.acme.security.tenant.in.dto.request;

import com.acme.security.tenant.dto.command.UpdateTenantCommand;

public record UpdateTenantRequest(
        String name,
        String slug,
        Boolean active
) {
    public UpdateTenantCommand toCommand(String id) {
        return new UpdateTenantCommand(id, name, slug, active);
    }
}


