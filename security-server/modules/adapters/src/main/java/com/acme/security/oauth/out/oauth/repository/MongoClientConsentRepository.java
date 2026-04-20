package com.acme.security.oauth.out.oauth.repository;

import com.acme.security.oauth.out.oauth.document.OAuth2AuthorizationConsentDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MongoClientConsentRepository extends MongoRepository<OAuth2AuthorizationConsentDocument, String> {

    Optional<OAuth2AuthorizationConsentDocument> findByRegisteredClientIdAndPrincipalName(
            String registeredClientId, String principalName);

    void deleteByRegisteredClientIdAndPrincipalName(
            String registeredClientId, String principalName);
}
