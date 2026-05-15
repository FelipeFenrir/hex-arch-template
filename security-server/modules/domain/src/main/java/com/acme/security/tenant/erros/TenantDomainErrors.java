package com.acme.security.tenant.erros;

import com.acme.shared.pattern.result.DomainError;

public class TenantDomainErrors {

    private TenantDomainErrors() {
        throw new IllegalStateException("Utility class");
    }

    public static DomainError tenantNotFound() {
        return new DomainError("TENANT_NOT_FOUND", "Tenant not found or inactive.");
    }

    public static DomainError tenantSlugAlreadyExists() {
        return new DomainError("TENANT_SLUG_ALREADY_EXISTS", "Tenant slug already exists.");
    }

    public static DomainError invalidTenantData() {
        return new DomainError("INVALID_TENANT_DATA", "Tenant data is invalid.");
    }

    public static DomainError tenantContextMissing() {
        return new DomainError("TENANT_CONTEXT_MISSING", "Tenant context is required for this request.");
    }
}
