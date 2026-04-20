package com.acme.security.user.service;

import com.acme.security.user.port.in.usecase.AuthenticateUserUseCase;
import com.acme.security.user.port.out.repository.UserCommandOutPort;
import com.acme.security.user.User;
import com.acme.security.user.errors.UserDomainErrors;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.vo.TenantId;

import java.util.Objects;

public class AuthenticateUserService implements AuthenticateUserUseCase {
    private final UserCommandOutPort userCommandOutPort;

    public AuthenticateUserService(UserCommandOutPort userCommandOutPort) {
        this.userCommandOutPort = Objects.requireNonNull(userCommandOutPort,
                "userCommandOutPort must not be null");
    }

    @Override
    public Result<User, DomainError> loadUserByUsername(String username, TenantId tenantId) {
        return userCommandOutPort.findByUsernameAndTenant(username, tenantId)
                .map(Result::<User, DomainError>success)
                .orElseGet(() -> Result.failure(UserDomainErrors.userNotFound()));
    }
}
