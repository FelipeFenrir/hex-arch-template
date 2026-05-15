package com.acme.security.user.service;

import com.acme.security.user.errors.UserDomainErrors;
import com.acme.security.user.port.in.usecase.DeleteUserUseCase;
import com.acme.security.user.port.out.repository.UserCommandOutPort;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.vo.TenantId;

import java.util.Objects;

public class DeleteUserService implements DeleteUserUseCase {

    private final UserCommandOutPort userCommandOutPort;

    public DeleteUserService(UserCommandOutPort userCommandOutPort) {
        this.userCommandOutPort = Objects.requireNonNull(userCommandOutPort,
                "userCommandOutPort must not be null");
    }

    @Override
    public Result<Void, DomainError> execute(String id, String tenantId) {
        TenantId resolvedTenant = TenantId.fromString(tenantId);

        if (!userCommandOutPort.existsByIdAndTenant(id, resolvedTenant)) {
            return Result.failure(UserDomainErrors.userNotFound());
        }

        userCommandOutPort.deleteByIdAndTenant(id, resolvedTenant);
        return Result.success(null);
    }
}

