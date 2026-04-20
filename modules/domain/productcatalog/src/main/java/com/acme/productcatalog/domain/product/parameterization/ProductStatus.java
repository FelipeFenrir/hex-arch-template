package com.acme.productcatalog.domain.product.parameterization;

import com.acme.productcatalog.domain.common.parametrizationflow.ParametrizationStatus;

import java.util.Arrays;

public enum ProductStatus implements ParametrizationStatus {
    DRAFT,
    WORKFORCE_VALIDATION,
    TEST_PERIOD,
    ACTIVE,
    INACTIVE;

    ProductStatus() {}

    public static ProductStatus fromName(String statusName) {
        return Arrays.stream(ProductStatus.values())
                .filter(
                        status -> status.name().equalsIgnoreCase(statusName)
                )
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Invalid parameterization status: " + statusName)
                );
    }
}