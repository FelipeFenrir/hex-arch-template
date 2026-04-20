package com.acme.security.user.port.in.usecase;

import com.acme.security.user.User;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.core.UseCase;
import com.acme.shared.vo.TenantId;

@UseCase
public interface AuthenticateUserUseCase {
    Result<User, DomainError> loadUserByUsername(String username, TenantId tenantId);
}
