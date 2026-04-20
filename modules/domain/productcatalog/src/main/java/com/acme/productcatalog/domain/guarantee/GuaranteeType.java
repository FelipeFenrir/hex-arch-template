package com.acme.productcatalog.domain.guarantee;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum GuaranteeType {
    INSURANCE_COVERAGE,
    INSURANCE_ASSISTANCE;

    public static GuaranteeType fromName(String guaranteeType) {
        return Arrays.stream(GuaranteeType.values())
                .filter(
                        type -> type.name().equalsIgnoreCase(guaranteeType)
                )
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Invalid Guarantee type: " + guaranteeType)
                );
    }
}
