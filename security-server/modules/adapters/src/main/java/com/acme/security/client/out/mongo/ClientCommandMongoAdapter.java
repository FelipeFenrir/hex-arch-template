package com.acme.security.client.out.mongo;

import com.acme.security.client.out.mongo.document.ClientDocument;
import com.acme.security.client.out.mongo.repository.MongoClientRepository;
import com.acme.security.client.port.out.repository.ClientCommandOutPort;
import com.acme.security.client.Client;
import com.acme.shared.stereotypes.adapter.OutputAdapter;
import com.acme.shared.vo.TenantId;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
@OutputAdapter
public class ClientCommandMongoAdapter implements ClientCommandOutPort {

    private final MongoClientRepository mongoClientRepository;

    public ClientCommandMongoAdapter(MongoClientRepository mongoClientRepository) {
        this.mongoClientRepository = Objects.requireNonNull(mongoClientRepository,
                "mongoClientRepository must not be null");
    }

    @Override
    public Client save(Client client) {
        var clientDocument = ClientDocument.of(client);
        return ClientDocument.map(mongoClientRepository.save(clientDocument));
    }

    @Override
    public Optional<Client> findByClientIdAndTenant(String clientId, TenantId tenantId) {
        return mongoClientRepository
                .findByClientIdAndTenantId(clientId, tenantId.stringValue())
                .map(ClientDocument::map);
    }

    @Override
    public Optional<Client> findById(String id) {
        return mongoClientRepository
                .findById(id)
                .map(ClientDocument::map);
    }
}
