package com.acme.security.tenant.erros;

import com.acme.shared.pattern.result.DomainError;

public class TenantDomainErrors {

    private TenantDomainErrors() {
        throw new IllegalStateException("Utility class");
    }

    public static DomainError tenantNotFound() {
        return new DomainError("TENANT_NOT_FOUND", "Tenant not found or inactive.");
    }
}
