package com.acme.security.tenant.port.in.usecase;

import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.core.UseCase;

@UseCase
public interface DeleteTenantUseCase {
    Result<Void, DomainError> execute(String id);
}

