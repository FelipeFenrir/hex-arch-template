package com.acme.personmdm.domain.common.enumerator;

import java.util.Arrays;

public enum PersonType {
    NP("NP", "Natural Person"),
    LP("LP", "Legal Person");

    private final String code;
    private final String label;

    public static PersonType fromCode(String code) {
        return Arrays.stream(PersonType.values())
                .filter(
                        personType -> personType.name().equalsIgnoreCase(code) ||
                                personType.code().equalsIgnoreCase(code)
                )
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Invalid person type: " + code)
                );
    }

    PersonType(final String code, final String label) {
        this.code = code;
        this.label = label;
    }

    public String code() {
        return code;
    }
    public String label() {
        return label;
    }
}
