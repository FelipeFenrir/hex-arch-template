package com.acme.security.client;

import com.acme.shared.vo.Id;
import com.acme.shared.vo.TenantId;

import java.time.Instant;
import java.util.Set;

public class Client {
    private final Id id;
    private final TenantId tenantId;
    private final String clientId;
    private final String clientSecret;
    private final Set<String> redirectUris;
    private final Set<String> scopes;
    private final Set<String> grantTypes;
    private Instant clientIdIssuedAt;
    private boolean active;

    private Client(Id id, TenantId tenantId, String clientId, String clientSecret,
                   Set<String> redirectUris, Set<String> scopes, Set<String> grantTypes,
                   boolean active) {
        this.id = id;
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUris = redirectUris;
        this.scopes = scopes;
        this.grantTypes = grantTypes;
        this.clientIdIssuedAt = Instant.now();
        this.active = active;
    }

    // Factory para novos Clients (ex: via tela de gestão)
    public static Client createNew(TenantId tenantId, String clientId, String encodedSecret,
                                   Set<String> redirects, Set<String> scopes, Set<String> grants) {
        return new Client(Id.withoutId(), tenantId, clientId, encodedSecret, redirects, scopes, grants, true);
    }

    // Re-hidratação do Objeto
    public static Client rehydrate(String id, String tenantId, String clientId, String secret,
                                   Set<String> redirects, Set<String> scopes, Set<String> grants,
                                   boolean active) {
        return new Client(Id.withId(id), TenantId.fromString(tenantId), clientId, secret, redirects, scopes, grants,
                active);
    }

    public void deactivate() {
        this.active = false;
    }

    // Getters
    public String idValue() {
        return id.stringfyId();
    }
    public String tenantValue() {
        return tenantId.stringValue();
    }
    public String clientId() {
        return clientId;
    }
    public String clientSecret() {
        return clientSecret;
    }
    public Set<String> redirectUris() {
        return redirectUris;
    }
    public Set<String> scopes() {
        return scopes;
    }
    public Set<String> grantTypes() {
        return grantTypes;
    }
    public boolean isActive() {
        return active;
    }
}
