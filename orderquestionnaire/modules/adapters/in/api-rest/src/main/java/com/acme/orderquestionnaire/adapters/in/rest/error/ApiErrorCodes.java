package com.acme.orderquestionnaire.adapters.in.rest.error;

public final class ApiErrorCodes {
    public static final String DOMAIN_PREFIX = "DOM-";
    public static final String VALIDATION_PREFIX = "VAL-";
    public static final String SYSTEM_PREFIX = "SYS-";

    public static final String VALIDATION_INVALID_PAYLOAD = VALIDATION_PREFIX + "001";
    public static final String VALIDATION_BAD_REQUEST = VALIDATION_PREFIX + "002";
    public static final String VALIDATION_MALFORMED_BODY = VALIDATION_PREFIX + "003";
    public static final String SYSTEM_UNEXPECTED_ERROR = SYSTEM_PREFIX + "001";

    private ApiErrorCodes() {
    }

    public static String domain(String domainCode) {
        return DOMAIN_PREFIX + domainCode;
    }
}

