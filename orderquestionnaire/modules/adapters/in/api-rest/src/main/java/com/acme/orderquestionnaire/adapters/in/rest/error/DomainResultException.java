package com.acme.orderquestionnaire.adapters.in.rest.error;

import com.acme.shared.pattern.result.DomainError;

import java.util.List;

public class DomainResultException extends RuntimeException {
    private final List<DomainError> errors;

    public DomainResultException(List<DomainError> errors) {
        super("Domain operation failed");
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public List<DomainError> errors() {
        return errors;
    }
}

