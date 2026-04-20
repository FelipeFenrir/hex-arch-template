package com.acme.personmdm.domain.common.enumerator;

import java.util.Arrays;

public enum ParameterizationStatus {
    DRAFT,
    ACTIVE,
    INACTIVE;

    public static ParameterizationStatus fromName(String statusName) {
        return Arrays.stream(ParameterizationStatus.values())
                .filter(
                        status -> status.name().equalsIgnoreCase(statusName)
                )
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Invalid parameterization status: " + statusName)
                );
    }

    ParameterizationStatus() {

    }
}