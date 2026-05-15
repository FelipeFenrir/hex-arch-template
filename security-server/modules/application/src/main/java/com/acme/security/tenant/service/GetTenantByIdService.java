package com.acme.security.tenant.service;

import com.acme.security.tenant.Tenant;
import com.acme.security.tenant.erros.TenantDomainErrors;
import com.acme.security.tenant.port.in.usecase.GetTenantByIdUseCase;
import com.acme.security.tenant.port.out.repository.TenantCommandOutPort;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.Objects;

public class GetTenantByIdService implements GetTenantByIdUseCase {

    private final TenantCommandOutPort tenantCommandOutPort;

    public GetTenantByIdService(TenantCommandOutPort tenantCommandOutPort) {
        this.tenantCommandOutPort = Objects.requireNonNull(tenantCommandOutPort,
                "tenantCommandOutPort must not be null");
    }

    @Override
    public Result<Tenant, DomainError> execute(String id) {
        return tenantCommandOutPort.findById(id)
                .map(Result::<Tenant, DomainError>success)
                .orElseGet(() -> Result.failure(TenantDomainErrors.tenantNotFound()));
    }
}

