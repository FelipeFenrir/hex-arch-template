package com.acme.security.client.port.in.usecase;

import com.acme.security.client.Client;
import com.acme.shared.stereotypes.core.UseCase;
import com.acme.shared.vo.TenantId;

import java.util.Optional;

@UseCase
public interface FindClientUseCase {
    Optional<Client> findById(String id);
    Optional<Client> findByClientId(String clientId, TenantId tenantId);
}
