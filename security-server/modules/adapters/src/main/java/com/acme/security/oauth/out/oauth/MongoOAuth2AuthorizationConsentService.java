package com.acme.security.oauth.out.oauth;

import com.acme.security.oauth.out.oauth.document.OAuth2AuthorizationConsentDocument;
import com.acme.security.oauth.out.oauth.repository.MongoClientConsentRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;

public class MongoOAuth2AuthorizationConsentService implements OAuth2AuthorizationConsentService {

    private final MongoClientConsentRepository mongoClientConsentRepository;

    public MongoOAuth2AuthorizationConsentService(MongoClientConsentRepository mongoClientConsentRepository) {
        this.mongoClientConsentRepository = Objects.requireNonNull(mongoClientConsentRepository,
                "mongoClientConsentRepository must not be null");
    }

    @Override
    public void save(OAuth2AuthorizationConsent consent) {
        // Criando o record com os dados do consentimento
        OAuth2AuthorizationConsentDocument doc = new OAuth2AuthorizationConsentDocument(
                null, // ID gerado pelo MongoDB
                consent.getRegisteredClientId(),
                consent.getPrincipalName(),
                consent.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet())
        );
        // Correção do nome da variável de repositório
        mongoClientConsentRepository.save(doc);
    }

    @Override
    public void remove(OAuth2AuthorizationConsent consent) {
        mongoClientConsentRepository.deleteByRegisteredClientIdAndPrincipalName(
                consent.getRegisteredClientId(), consent.getPrincipalName());
    }

    @Override
    public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
        return mongoClientConsentRepository.findByRegisteredClientIdAndPrincipalName(registeredClientId, principalName)
                .map(this::toDomain)
                .orElse(null);
    }

    private OAuth2AuthorizationConsent toDomain(OAuth2AuthorizationConsentDocument doc) {
        OAuth2AuthorizationConsent.Builder builder = OAuth2AuthorizationConsent
                .withId(doc.registeredClientId(), doc.principalName());

        if (doc.authorities() != null) {
            for (String authority : doc.authorities()) {
                builder.authority(new SimpleGrantedAuthority(authority));
            }
        }

        return builder.build();
    }
}

