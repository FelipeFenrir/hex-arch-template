package com.acme.productcatalog.domain.exception;

public class TransitionStatusNotFoundException extends RuntimeException {
    public TransitionStatusNotFoundException(String message) {
        super(message);
    }
}
