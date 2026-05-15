package com.acme.security.tenant.dto.view;

import com.acme.security.tenant.Tenant;

public record TenantView(
        String id,
        String name,
        String slug,
        boolean active
) {
    public static TenantView from(Tenant tenant) {
        return new TenantView(
                tenant.idValue(),
                tenant.name(),
                tenant.slug(),
                tenant.isActive()
        );
    }
}

