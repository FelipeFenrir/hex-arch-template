package com.acme.personmdm.domain.common.enumerator;

import java.util.Arrays;

public enum BiologicalSex {
    FEMALE("FEMALE", "Female", "F"),
    MALE("MALE", "Male", "M");

    private final String code;
    private final String label;
    private final String singleCode;

    public static BiologicalSex fromCode(String code) {
        return Arrays.stream(BiologicalSex.values())
                .filter(
                        biologicalSex -> biologicalSex.name().equalsIgnoreCase(code)
                                || biologicalSex.code().equalsIgnoreCase(code)
                                || biologicalSex.singleCode().equalsIgnoreCase(code)
                )
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid biological sex: " + code));

    }

    BiologicalSex(final String code, final String label, final String singleCode) {
        this.code = code;
        this.label = label;
        this.singleCode = singleCode;
    }

    public String code() {
        return code;
    }
    public String label() {
        return label;
    }
    public String singleCode() {
        return singleCode;
    }
}
