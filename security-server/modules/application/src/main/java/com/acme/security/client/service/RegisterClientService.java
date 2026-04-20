package com.acme.security.client.service;

import com.acme.security.client.dto.command.ClientRegistrationCommand;
import com.acme.security.client.port.in.usecase.RegisterClientUseCase;
import com.acme.security.client.port.out.repository.ClientCommandOutPort;
import com.acme.security.security.port.out.engine.PasswordEncoderPort;
import com.acme.security.client.Client;
import com.acme.security.client.errors.ClientDomainErrors;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.vo.TenantId;

import java.util.Objects;

public class RegisterClientService implements RegisterClientUseCase {

    private final ClientCommandOutPort clientCommandOutPort;
    private final PasswordEncoderPort passwordEncoder;

    public RegisterClientService(ClientCommandOutPort clientCommandOutPort, PasswordEncoderPort passwordEncoder) {
        this.clientCommandOutPort = Objects.requireNonNull(clientCommandOutPort,
                "clientCommandOutPort must not be null");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder,
                "passwordEncoder must not be null");
    }

    @Override
    public Result<Client, DomainError> execute(ClientRegistrationCommand command) {
        TenantId tenantId = TenantId.fromString(command.tenantId());

        if (clientCommandOutPort.findByClientIdAndTenant(command.clientId(), tenantId).isPresent()) {
            return Result.failure(ClientDomainErrors.clientAlreadyExists());
        }

        // Clients também usam Argon2 no secret para Client-Server auth
        final String encodedSecret = passwordEncoder.encode(command.rawSecret());

        Client newClient = Client.createNew(
                tenantId,
                command.clientId(),
                encodedSecret,
                command.redirectUris(),
                command.scopes(),
                command.grantTypes()
        );

        return Result.success(clientCommandOutPort.save(newClient));
    }
}
