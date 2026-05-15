package com.acme.security.client.out.mongo.repository;

import com.acme.security.client.out.mongo.document.ClientDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MongoClientRepository extends MongoRepository<ClientDocument, String> {
    Optional<ClientDocument> findByClientIdAndTenantId(String clientId, String tenantId);
    List<ClientDocument> findAllByTenantId(String tenantId);
}
