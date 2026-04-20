package com.acme.productcatalog.domain.product;

import com.acme.productcatalog.domain.product.decorator.ProductCapability;
import com.acme.productcatalog.domain.product.parameterization.ProductStatus;
import com.acme.shared.vo.Id;

import java.time.Instant;
import java.util.Set;

public final class InsuranceProduct implements Product {
    private final Id id;
    private final String sourceId;
    private final String name;
    private final String surname;
    private final String description;
    private final ProductKind productKind = ProductKind.INSURANCE;
    private final ProductStatus productStatus;
    private final Set<ProductCapability> productCapabilities;
    private final Instant createdAt;
    private final Instant updatedAt;

    private InsuranceProduct(final Id id, final String sourceId, final String name, final String surname,
                           final String description, final ProductStatus productStatus,
                           final Set<ProductCapability> productCapabilities, final Instant createdAt,
                           final Instant updatedAt) {

        this.id = id;
        this.sourceId = sourceId;
        this.name = name;
        this.surname = surname;
        this.description = description;
        this.productStatus = productStatus;
        this.productCapabilities = productCapabilities;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static InsuranceProduct createNew(final Id id, final String sourceId, final String name, final String surname,
                                           final String description, Set<ProductCapability> productCapabilities) {
        Instant now = Instant.now();
        return new InsuranceProduct(id, sourceId, name, surname, description, ProductStatus.DRAFT,
                productCapabilities, now, now);
    }

    @Override public Id id() {
        return id;
    }
    @Override public String sourceId() {
        return sourceId;
    }
    @Override public String name() {
        return name;
    }
    @Override public String surname() {
        return surname;
    }
    @Override public String description() {
        return description;
    }
    @Override public ProductKind productKind() {
        return this.productKind;
    }
    @Override public ProductStatus status() {
        return productStatus;
    }
    @Override public Set<ProductCapability> productCapabilities() {
        return productCapabilities;
    }
    @Override public Instant createdAt() {
        return createdAt;
    }
    @Override public Instant updatedAt() {
        return updatedAt;
    }
}
