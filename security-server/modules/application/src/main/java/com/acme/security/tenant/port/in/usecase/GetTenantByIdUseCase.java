package com.acme.security.tenant.port.in.usecase;

import com.acme.security.tenant.Tenant;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.core.UseCase;

@UseCase
public interface GetTenantByIdUseCase {
    Result<Tenant, DomainError> execute(String id);
}

