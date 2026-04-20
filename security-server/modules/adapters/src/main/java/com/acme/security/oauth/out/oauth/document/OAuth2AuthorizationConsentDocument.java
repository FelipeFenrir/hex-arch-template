package com.acme.security.oauth.out.oauth.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "oauth2_consent")
@CompoundIndex(name = "consent_idx", def = "{'registeredClientId': 1, 'principalName': 1}", unique = true)
public record OAuth2AuthorizationConsentDocument(
        @Id
        String id,
        String registeredClientId,
        String principalName,
        Set<String> authorities
) {
}
