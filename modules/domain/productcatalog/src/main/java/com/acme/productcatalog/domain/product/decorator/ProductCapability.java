package com.acme.productcatalog.domain.product.decorator;

public sealed interface ProductCapability permits
        ServiceCapability, BundleCapability, InsuranceCapability, GuaranteeCapability {
}
