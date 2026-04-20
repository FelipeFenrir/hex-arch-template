package com.acme.security.client.errors;

import com.acme.shared.pattern.result.DomainError;

public final class ClientDomainErrors {
    private ClientDomainErrors() {
        throw new IllegalStateException("Utility class");
    }

    public static DomainError clientAlreadyExists() {
        return new DomainError("CLIENT_ALREADY_EXISTS", "Client already registered.");
    }
}
