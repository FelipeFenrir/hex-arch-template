package com.acme.security.oauth.out.oauth.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Document(collection = "oauth2_authorizations")
public record OAuth2AuthorizationDocument(
        @Id
        String id,
        String registeredClientId,
        String principalName,
        String authorizationGrantType,
        Set<String> authorizedScopes,
        String attributes,
        String state,

        // Authorization Code
        String authorizationCodeValue,
        Instant authorizationCodeIssuedAt,
        Instant authorizationCodeExpiresAt,

        // Access Token
        String accessTokenValue,
        Instant accessTokenIssuedAt,
        Instant accessTokenExpiresAt,
        Set<String> accessTokenScopes,

        // Refresh Token
        String refreshTokenValue,
        Instant refreshTokenIssuedAt,
        Instant refreshTokenExpiresAt
) {}
