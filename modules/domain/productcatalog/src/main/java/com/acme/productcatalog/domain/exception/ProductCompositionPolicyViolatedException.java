package com.acme.productcatalog.domain.exception;

import java.util.List;

public final class ProductCompositionPolicyViolatedException extends RuntimeException {
    public ProductCompositionPolicyViolatedException(String message) {
        super(message);
    }
    public ProductCompositionPolicyViolatedException(List<String> errors) {
        super("Product Composition Policy validation fail:\n" + String.join("\n", errors));
    }
}
