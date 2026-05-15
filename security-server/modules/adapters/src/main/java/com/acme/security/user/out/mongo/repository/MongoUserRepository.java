package com.acme.security.user.out.mongo.repository;

import com.acme.security.user.out.mongo.document.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MongoUserRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByUsernameAndTenantId(String username, String tenantId);
    Optional<UserDocument> findByIdAndTenantId(String id, String tenantId);
    List<UserDocument> findAllByTenantId(String tenantId);
    boolean existsByIdAndTenantId(String id, String tenantId);
    void deleteByIdAndTenantId(String id, String tenantId);
}
