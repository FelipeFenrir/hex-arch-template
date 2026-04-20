package com.acme.productcatalog.domain.exception;

public final class EmptyBundleProductException extends RuntimeException {
    public EmptyBundleProductException(String message) {
        super(message);
    }
}
