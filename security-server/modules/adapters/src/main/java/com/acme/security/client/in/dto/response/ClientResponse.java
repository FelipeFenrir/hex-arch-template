package com.acme.security.client.in.dto.response;

import com.acme.security.client.dto.view.ClientView;

import java.util.Set;

public record ClientResponse(
        String id,
        String tenantId,
        String clientId,
        Set<String> redirectUris,
        Set<String> scopes,
        Set<String> grantTypes,
        boolean active
) {
    public static ClientResponse from(ClientView view) {
        return new ClientResponse(
                view.id(),
                view.tenantId(),
                view.clientId(),
                view.redirectUris(),
                view.scopes(),
                view.grantTypes(),
                view.active()
        );
    }
}

