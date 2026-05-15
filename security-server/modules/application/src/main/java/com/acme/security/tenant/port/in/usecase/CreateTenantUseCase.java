package com.acme.security.tenant.port.in.usecase;

import com.acme.security.tenant.Tenant;
import com.acme.security.tenant.dto.command.CreateTenantCommand;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.core.UseCase;

@UseCase
public interface CreateTenantUseCase {
    Result<Tenant, DomainError> execute(CreateTenantCommand command);
}

