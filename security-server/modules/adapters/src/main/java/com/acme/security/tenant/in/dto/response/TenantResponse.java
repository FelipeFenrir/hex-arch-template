package com.acme.security.tenant.in.dto.response;

import com.acme.security.tenant.dto.view.TenantView;

public record TenantResponse(
        String id,
        String name,
        String slug,
        boolean active
) {
    public static TenantResponse from(TenantView view) {
        return new TenantResponse(
                view.id(),
                view.name(),
                view.slug(),
                view.active()
        );
    }
}


