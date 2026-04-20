package com.acme.security.client.in.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Set;

public record CreateClientRequest(
        @JsonProperty("clientId") String clientId,
        @JsonProperty("clientSecret") String clientSecret,
        @JsonProperty("redirectUris") String redirectUris,
        @JsonProperty("scopes") String scopes,
        @JsonProperty("grantTypes") String grantTypes
) {
}
