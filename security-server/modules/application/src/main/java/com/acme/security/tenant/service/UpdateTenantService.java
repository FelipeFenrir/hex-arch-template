package com.acme.security.tenant.service;

import com.acme.security.tenant.Tenant;
import com.acme.security.tenant.dto.command.UpdateTenantCommand;
import com.acme.security.tenant.erros.TenantDomainErrors;
import com.acme.security.tenant.port.in.usecase.UpdateTenantUseCase;
import com.acme.security.tenant.port.out.repository.TenantCommandOutPort;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.Objects;

public class UpdateTenantService implements UpdateTenantUseCase {

    private final TenantCommandOutPort tenantCommandOutPort;

    public UpdateTenantService(TenantCommandOutPort tenantCommandOutPort) {
        this.tenantCommandOutPort = Objects.requireNonNull(tenantCommandOutPort,
                "tenantCommandOutPort must not be null");
    }

    @Override
    public Result<Tenant, DomainError> execute(UpdateTenantCommand command) {
        if (command == null || isBlank(command.id())) {
            return Result.failure(TenantDomainErrors.invalidTenantData());
        }

        var existingOpt = tenantCommandOutPort.findById(command.id());
        if (existingOpt.isEmpty()) {
            return Result.failure(TenantDomainErrors.tenantNotFound());
        }

        Tenant existing = existingOpt.get();
        String name = isBlank(command.name()) ? existing.name() : command.name().trim();
        String slug = isBlank(command.slug()) ? existing.slug() : command.slug().trim().toLowerCase();

        if (!slug.equals(existing.slug()) && tenantCommandOutPort.existsBySlug(slug)) {
            return Result.failure(TenantDomainErrors.tenantSlugAlreadyExists());
        }

        boolean active = command.active() == null ? existing.isActive() : command.active();

        Tenant updated = Tenant.rehydrate(existing.idValue(), name, slug, active);
        return Result.success(tenantCommandOutPort.save(updated));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

