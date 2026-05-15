package com.acme.security.user.port.in.usecase;

import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.core.UseCase;

@UseCase
public interface DeleteUserUseCase {
    Result<Void, DomainError> execute(String id, String tenantId);
}

