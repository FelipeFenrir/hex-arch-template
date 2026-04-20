package com.acme.personmdm.domain.common.enumerator;

import java.util.Arrays;

public enum ContactMethodType {
    PHONE("phone"),
    EMAIL("email"),
    INSTANT_MESSENGER("instant_messenger"),
    SOCIAL_NETWORK("social_network"),
    SOCIAL_MIDIA("social_midia");

    private final String code;

    public static ContactMethodType fromCode(String code) {
        return Arrays.stream(ContactMethodType.values())
                .filter(
                        contactMethodType -> contactMethodType.name().equalsIgnoreCase(code) ||
                                contactMethodType.code().equalsIgnoreCase(code)
                )
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Invalid contact method type: " + code)
                );
    }

    ContactMethodType(final String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
