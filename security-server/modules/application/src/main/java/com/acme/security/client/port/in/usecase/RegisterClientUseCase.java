package com.acme.security.client.port.in.usecase;

import com.acme.security.client.dto.command.ClientRegistrationCommand;
import com.acme.security.client.Client;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.core.UseCase;

@UseCase
public interface RegisterClientUseCase {
    Result<Client, DomainError> execute(ClientRegistrationCommand command);
}
