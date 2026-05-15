package com.acme.security.user.service;

import com.acme.security.security.port.out.engine.PasswordEncoderPort;
import com.acme.security.user.User;
import com.acme.security.user.dto.command.UpdateUserCommand;
import com.acme.security.user.errors.UserDomainErrors;
import com.acme.security.user.port.in.usecase.UpdateUserUseCase;
import com.acme.security.user.port.out.repository.UserCommandOutPort;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.vo.TenantId;

import java.util.Objects;
import java.util.Set;

public class UpdateUserService implements UpdateUserUseCase {

    private final UserCommandOutPort userCommandOutPort;
    private final PasswordEncoderPort passwordEncoderPort;

    public UpdateUserService(UserCommandOutPort userCommandOutPort,
                             PasswordEncoderPort passwordEncoderPort) {
        this.userCommandOutPort = Objects.requireNonNull(userCommandOutPort,
                "userCommandOutPort must not be null");
        this.passwordEncoderPort = Objects.requireNonNull(passwordEncoderPort,
                "passwordEncoderPort must not be null");
    }

    @Override
    public Result<User, DomainError> execute(UpdateUserCommand command) {
        TenantId tenantId = TenantId.fromString(command.tenantId());

        var existingOpt = userCommandOutPort.findByIdAndTenant(command.id(), tenantId);
        if (existingOpt.isEmpty()) {
            return Result.failure(UserDomainErrors.userNotFound());
        }

        User existing = existingOpt.get();

        String username = normalizeUsername(command.username(), existing.username());
        if (username.isBlank()) {
            return Result.failure(UserDomainErrors.invalidUsername());
        }

        if (!username.equals(existing.username())
                && userCommandOutPort.findByUsernameAndTenant(username, tenantId).isPresent()) {
            return Result.failure(UserDomainErrors.usernameAlreadyExists());
        }

        String encodedPassword = existing.password();
        if (command.password() != null && !command.password().isBlank()) {
            String newEncodedPassword = passwordEncoderPort.encode(command.password());
            var passwordChangeResult = existing.changePassword(newEncodedPassword);
            if (passwordChangeResult.isFailure()) {
                return Result.failure(passwordChangeResult.fold(ignored -> null, error -> error));
            }
            encodedPassword = newEncodedPassword;
        }

        Set<String> roles = (command.roles() == null || command.roles().isEmpty())
                ? existing.roles()
                : command.roles();
        boolean active = command.active() == null ? existing.isActive() : command.active();

        User updated = User.rehydrate(
                existing.idValue(),
                existing.tenantValue(),
                username,
                encodedPassword,
                roles,
                active
        );

        return Result.success(userCommandOutPort.save(updated));
    }

    private static String normalizeUsername(String candidate, String fallback) {
        return candidate == null ? fallback : candidate.trim();
    }
}


