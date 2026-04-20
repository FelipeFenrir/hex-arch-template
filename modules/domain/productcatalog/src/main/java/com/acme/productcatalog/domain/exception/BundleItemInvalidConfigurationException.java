package com.acme.productcatalog.domain.exception;

import java.util.List;

public class BundleItemInvalidConfigurationException extends RuntimeException {
    public BundleItemInvalidConfigurationException(String message) {
        super(message);
    }
    public BundleItemInvalidConfigurationException(List<String> errors) {
        super("Bundle Item configuration fail:\n" + String.join("\n", errors));
    }
}
