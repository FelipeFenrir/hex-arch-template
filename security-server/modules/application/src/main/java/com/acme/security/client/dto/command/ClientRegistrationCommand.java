package com.acme.security.client.dto.command;

import java.util.Set;

public record ClientRegistrationCommand(
        String tenantId,
        String clientId,
        String rawSecret,
        Set<String> redirectUris,
        Set<String> scopes,
        Set<String> grantTypes
) { }
