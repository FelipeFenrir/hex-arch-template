package com.acme.security.tenant.service;

import com.acme.security.tenant.erros.TenantDomainErrors;
import com.acme.security.tenant.port.in.usecase.DeleteTenantUseCase;
import com.acme.security.tenant.port.out.repository.TenantCommandOutPort;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.Objects;

public class DeleteTenantService implements DeleteTenantUseCase {

    private final TenantCommandOutPort tenantCommandOutPort;

    public DeleteTenantService(TenantCommandOutPort tenantCommandOutPort) {
        this.tenantCommandOutPort = Objects.requireNonNull(tenantCommandOutPort,
                "tenantCommandOutPort must not be null");
    }

    @Override
    public Result<Void, DomainError> execute(String id) {
        if (id == null || id.isBlank()) {
            return Result.failure(TenantDomainErrors.invalidTenantData());
        }

        if (tenantCommandOutPort.findById(id).isEmpty()) {
            return Result.failure(TenantDomainErrors.tenantNotFound());
        }

        tenantCommandOutPort.deleteById(id);
        return Result.success(null);
    }
}

