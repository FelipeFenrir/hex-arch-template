package com.acme.security.oauth.out.oauth;

import com.acme.security.oauth.out.oauth.document.OAuth2AuthorizationDocument;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

public class MongoOAuth2AuthorizationService implements OAuth2AuthorizationService {

    private final MongoTemplate mongoTemplate;
    private final RegisteredClientRepository registeredClientRepository;
    private final ObjectMapper oauth2ObjectMapper;

    public MongoOAuth2AuthorizationService(MongoTemplate mongoTemplate,
                                           RegisteredClientRepository registeredClientRepository,
                                           ObjectMapper oauth2ObjectMapper) {
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate, "mongoTemplate must not be null");
        this.registeredClientRepository = Objects.requireNonNull(registeredClientRepository, "registeredClientRepository must not be null");
        this.oauth2ObjectMapper = Objects.requireNonNull(oauth2ObjectMapper, "oauth2ObjectMapper must not be null");
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        mongoTemplate.save(toDocument(authorization));
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        mongoTemplate.remove(
                new Query(Criteria.where("id").is(authorization.getId())),
                OAuth2AuthorizationDocument.class
        );
    }

    @Override
    public OAuth2Authorization findById(String id) {
        OAuth2AuthorizationDocument doc = mongoTemplate.findById(id, OAuth2AuthorizationDocument.class);
        return doc != null ? toDomain(doc) : null;
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        String field = resolveTokenField(tokenType);
        Query query = new Query(Criteria.where(field).is(token));
        OAuth2AuthorizationDocument doc = mongoTemplate.findOne(query, OAuth2AuthorizationDocument.class);
        return doc != null ? toDomain(doc) : null;
    }

    private String resolveTokenField(OAuth2TokenType tokenType) {
        if (tokenType != null && OAuth2ParameterNames.STATE.equals(tokenType.getValue())) return "state";
        if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) return "accessTokenValue";
        if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) return "refreshTokenValue";
        return "authorizationCodeValue";
    }

    private String writeMap(Map<String, Object> data) {
        try {
            return this.oauth2ObjectMapper.writeValueAsString(data);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    private OAuth2AuthorizationDocument toDocument(OAuth2Authorization auth) {
        var authCodeToken = auth.getToken(OAuth2AuthorizationCode.class);
        var accessToken = auth.getAccessToken();
        var refreshToken = auth.getRefreshToken();

        return new OAuth2AuthorizationDocument(
                auth.getId(),
                auth.getRegisteredClientId(),
                auth.getPrincipalName(),
                auth.getAuthorizationGrantType().getValue(),
                auth.getAuthorizedScopes(),
                writeMap(auth.getAttributes()),
                auth.getAttribute(OAuth2ParameterNames.STATE),

                // Code
                authCodeToken != null ? authCodeToken.getToken().getTokenValue() : null,
                authCodeToken != null ? authCodeToken.getToken().getIssuedAt() : null,
                authCodeToken != null ? authCodeToken.getToken().getExpiresAt() : null,

                // Access
                accessToken != null ? accessToken.getToken().getTokenValue() : null,
                accessToken != null ? accessToken.getToken().getIssuedAt() : null,
                accessToken != null ? accessToken.getToken().getExpiresAt() : null,
                accessToken != null ? accessToken.getToken().getScopes() : null,

                // Refresh
                refreshToken != null ? refreshToken.getToken().getTokenValue() : null,
                refreshToken != null ? refreshToken.getToken().getIssuedAt() : null,
                refreshToken != null ? refreshToken.getToken().getExpiresAt() : null
        );
    }

    private Map<String, Object> parseMap(String data) {
        try {
            return this.oauth2ObjectMapper.readValue(data, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    private OAuth2Authorization toDomain(OAuth2AuthorizationDocument doc) {
        RegisteredClient registeredClient = registeredClientRepository.findById(doc.registeredClientId());
        if (registeredClient == null) {
            throw new DataRetrievalFailureException("RegisteredClient not found: " + doc.registeredClientId());
        }

        OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .id(doc.id())
                .principalName(doc.principalName())
                .authorizationGrantType(new AuthorizationGrantType(doc.authorizationGrantType()))
                .authorizedScopes(doc.authorizedScopes())
                .attributes(attrs -> attrs.putAll(parseMap(doc.attributes())));

        // Reconstrução do Authorization Code
        if (doc.authorizationCodeValue() != null) {
            OAuth2AuthorizationCode code = new OAuth2AuthorizationCode(
                    doc.authorizationCodeValue(), doc.authorizationCodeIssuedAt(), doc.authorizationCodeExpiresAt());
            builder.token(code);
        }

        // Reconstrução do Access Token
        if (doc.accessTokenValue() != null) {
            OAuth2AccessToken accessToken = new OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    doc.accessTokenValue(),
                    doc.accessTokenIssuedAt(),
                    doc.accessTokenExpiresAt(),
                    doc.accessTokenScopes());
            builder.token(accessToken);
        }

        // Reconstrução do Refresh Token (Correção da IDE)
        if (doc.refreshTokenValue() != null) {
            OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(
                    doc.refreshTokenValue(),
                    doc.refreshTokenIssuedAt(),
                    doc.refreshTokenExpiresAt());
            builder.refreshToken(refreshToken);
        }

        return builder.build();
    }
}
