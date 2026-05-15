package com.acme.security.client.in.dto.request;

import com.acme.security.client.dto.command.ClientRegistrationCommand;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record CreateClientRequest(
        @JsonProperty("clientId") String clientId,
        @JsonProperty("clientSecret") String clientSecret,
        @JsonProperty("redirectUris") String redirectUris,
        @JsonProperty("scopes") String scopes,
        @JsonProperty("grantTypes") String grantTypes
) {
    public ClientRegistrationCommand toCommand(String tenantId) {
        return new ClientRegistrationCommand(
                tenantId,
                clientId,
                clientSecret,
                toSet(redirectUris),
                toSet(scopes),
                toSet(grantTypes)
        );
    }

    private static Set<String> toSet(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }

        return Stream.of(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .collect(Collectors.toSet());
    }
}
