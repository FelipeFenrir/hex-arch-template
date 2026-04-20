package com.acme.security.client.service;

import com.acme.security.client.port.in.usecase.FindClientUseCase;
import com.acme.security.client.port.out.repository.ClientCommandOutPort;
import com.acme.security.client.Client;
import com.acme.shared.vo.TenantId;

import java.util.Objects;
import java.util.Optional;

public class FindClientService implements FindClientUseCase {
    private final ClientCommandOutPort clientCommandOutPort;

    public FindClientService(ClientCommandOutPort clientCommandOutPort) {
        this.clientCommandOutPort = Objects.requireNonNull(clientCommandOutPort,
                "clientCommandOutPort must not be null");
    }

    @Override
    public Optional<Client> findById(String id) {
        return clientCommandOutPort.findById(id);
    }

    @Override
    public Optional<Client> findByClientId(String clientId, TenantId tenantId) {
        return clientCommandOutPort.findByClientIdAndTenant(clientId, tenantId);
    }
}