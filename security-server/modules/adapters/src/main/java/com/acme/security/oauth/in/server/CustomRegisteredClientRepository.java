package com.acme.security.oauth.in.server;

import com.acme.security.client.port.in.usecase.FindClientUseCase;
import com.acme.security.client.Client;
import com.acme.shared.TenantContextHolder;
import com.acme.shared.vo.TenantId;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

@Component
public class CustomRegisteredClientRepository implements RegisteredClientRepository {

    private final FindClientUseCase findClientUseCase;

    public CustomRegisteredClientRepository(FindClientUseCase findClientUseCase) {
        this.findClientUseCase = Objects.requireNonNull(findClientUseCase,
                "findClientUseCase must not be null");
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        // Opcional: implementar se quiser que o Spring salve diretamente
    }

    @Override
    public RegisteredClient findById(String id) {
        return findClientUseCase.findById(id)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {

        // Aqui o TenantContext.getTenant() deve estar preenchido pelo filtro
        TenantId tenantId = TenantId.fromString(TenantContextHolder.currentTenant());

        return findClientUseCase.findByClientId(clientId, tenantId)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    // Mapper: Domínio -> Spring
    private RegisteredClient toRegisteredClient(Client client) {
        return RegisteredClient
                .withId(client.idValue())
                .clientId(client.clientId())
                .clientSecret(client.clientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST) // Recomendado adicionar ambos
                .authorizationGrantTypes(
                        grants -> client.grantTypes().forEach(
                                grant -> grants.add(new AuthorizationGrantType(grant))
                        )
                )
                .redirectUris(
                        uris -> uris.addAll(client.redirectUris())
                )
                .scopes(
                        scopes -> scopes.addAll(client.scopes())
                )
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        .build())
                .tokenSettings(
                        TokenSettings.builder()
                                .accessTokenTimeToLive(Duration.ofHours(1))
                                .build()
                )
                .build();
    }
}
