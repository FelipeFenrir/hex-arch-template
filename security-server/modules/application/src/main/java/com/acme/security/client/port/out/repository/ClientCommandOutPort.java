package com.acme.security.client.port.out.repository;

import com.acme.security.client.Client;
import com.acme.shared.stereotypes.core.OutputPort;
import com.acme.shared.vo.TenantId;

import java.util.Optional;

@OutputPort
public interface ClientCommandOutPort {
    Client save(Client client);
    Optional<Client> findByClientIdAndTenant(String clientId, TenantId tenantId);
    Optional<Client> findById(String id);
}
