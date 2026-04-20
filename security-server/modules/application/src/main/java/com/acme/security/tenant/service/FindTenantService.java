package com.acme.security.tenant.service;

import com.acme.security.tenant.port.in.usecase.FindTenantUseCase;
import com.acme.security.tenant.port.out.repository.TenantCommandOutPort;
import com.acme.security.tenant.Tenant;
import com.acme.security.tenant.erros.TenantDomainErrors;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.Objects;

public class FindTenantService implements FindTenantUseCase {

    private final TenantCommandOutPort tenantCommandOutPort;

    public FindTenantService(TenantCommandOutPort tenantCommandOutPort) {
        this.tenantCommandOutPort = Objects.requireNonNull(tenantCommandOutPort,
                "tenantCommandOutPort must not be null");
    }

    @Override
    public Result<Tenant, DomainError> findBySlug(String slug) {
        return tenantCommandOutPort.findBySlug(slug)
                .filter(Tenant::isActive)
                .map(Result::<Tenant, DomainError>success)
                .orElseGet(() -> Result.failure(TenantDomainErrors.tenantNotFound()));
    }
}
