package com.acme.security.client.dto.view;

import com.acme.security.client.Client;

import java.util.Set;

public record ClientView(
        String id,
        String tenantId,
        String clientId,
        Set<String> redirectUris,
        Set<String> scopes,
        Set<String> grantTypes,
        boolean active
) {
    public static ClientView from(Client client) {
        return new ClientView(
                client.idValue(),
                client.tenantValue(),
                client.clientId(),
                client.redirectUris(),
                client.scopes(),
                client.grantTypes(),
                client.isActive()
        );
    }
}

