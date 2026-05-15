package com.acme.security.user.service;

import com.acme.security.user.dto.command.UserRegistrationCommand;
import com.acme.security.user.port.in.usecase.RegisterUserUseCase;
import com.acme.security.security.port.out.engine.PasswordEncoderPort;
import com.acme.security.user.port.out.repository.UserCommandOutPort;
import com.acme.security.user.User;
import com.acme.security.user.errors.UserDomainErrors;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.vo.TenantId;

import java.util.Objects;
import java.util.Set;

public class RegisterUserService implements RegisterUserUseCase {

    private final UserCommandOutPort userCommandOutPort;
    private final PasswordEncoderPort passwordEncoder;

    public RegisterUserService(UserCommandOutPort userCommandOutPort, PasswordEncoderPort passwordEncoder) {
        this.userCommandOutPort = Objects.requireNonNull(userCommandOutPort,
                "userCommandOutPort must not be null");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder,
                "passwordEncoder must not be null");
    }

    @Override
    public Result<User, DomainError> execute(UserRegistrationCommand command) {
        TenantId tenantId = TenantId.fromString(command.tenantId());

        // 1. Verifica se existe (Porta de saída também retorna Result ou Booleano)
        if (userCommandOutPort.findByUsernameAndTenant(command.username(), tenantId).isPresent()) {
            return Result.failure(UserDomainErrors.usernameAlreadyExists());
        }

        // 2. Processa a senha
        final String encodedPassword = passwordEncoder.encode(command.password());

        Set<String> roles = (command.roles() == null || command.roles().isEmpty())
                ? Set.of("ROLE_USER")
                : command.roles();

        // 3. Cria o usuário
        User newUser = User.createNew(tenantId, command.username(), encodedPassword, roles);

        // 4. Salva
        return Result.success(userCommandOutPort.save(newUser));
    }
}
