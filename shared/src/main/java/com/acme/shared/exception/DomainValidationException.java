package com.acme.shared.exception;

import java.util.List;

public class DomainValidationException extends IllegalArgumentException {

    public DomainValidationException(String message) {
        super(message);
    }

    public DomainValidationException(List<String> errors) {
        super("Domain validation fail:\n" + String.join("\n", errors));
    }

}
