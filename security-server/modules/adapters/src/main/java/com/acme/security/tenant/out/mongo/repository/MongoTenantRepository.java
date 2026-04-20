package com.acme.security.tenant.out.mongo.repository;

import com.acme.security.tenant.out.mongo.document.TenantDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MongoTenantRepository extends MongoRepository<TenantDocument, String> {

    // Busca o documento pelo slug (que usamos no subdomínio)
    Optional<TenantDocument> findBySlug(String slug);

    boolean existsBySlug(String slug);
}