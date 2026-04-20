package com.acme.productcatalog.domain.product;

import com.acme.productcatalog.domain.product.parameterization.ProductStatus;
import com.acme.productcatalog.domain.product.decorator.ProductCapability;
import com.acme.shared.vo.Id;

import java.time.Instant;
import java.util.Set;

public sealed interface Product permits SimpleProduct, BundleProduct, ServiceProduct, InsuranceProduct {
    Id id();
    String sourceId();
    String name();
    String surname();
    String description();
    ProductKind productKind();
    ProductStatus status();
    Set<ProductCapability> productCapabilities();
    Instant createdAt();
    Instant updatedAt();

    //void addProductComponent(ProductComponent component, ProductCompositionPolicies compositionPolicies);
}
