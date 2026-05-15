package com.acme.security.tenant.service;

import com.acme.security.tenant.Tenant;
import com.acme.security.tenant.dto.command.CreateTenantCommand;
import com.acme.security.tenant.erros.TenantDomainErrors;
import com.acme.security.tenant.port.in.usecase.CreateTenantUseCase;
import com.acme.security.tenant.port.out.repository.TenantCommandOutPort;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.Objects;

public class CreateTenantService implements CreateTenantUseCase {

    private final TenantCommandOutPort tenantCommandOutPort;

    public CreateTenantService(TenantCommandOutPort tenantCommandOutPort) {
        this.tenantCommandOutPort = Objects.requireNonNull(tenantCommandOutPort,
                "tenantCommandOutPort must not be null");
    }

    @Override
    public Result<Tenant, DomainError> execute(CreateTenantCommand command) {
        if (isBlank(command.id()) || isBlank(command.name()) || isBlank(command.slug())) {
            return Result.failure(TenantDomainErrors.invalidTenantData());
        }

        String slug = command.slug().trim().toLowerCase();
        if (tenantCommandOutPort.existsBySlug(slug)) {
            return Result.failure(TenantDomainErrors.tenantSlugAlreadyExists());
        }

        Tenant tenant = Tenant.createNew(command.id().trim(), command.name().trim(), slug);
        return Result.success(tenantCommandOutPort.save(tenant));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

