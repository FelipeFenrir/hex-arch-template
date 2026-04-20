package com.acme.productcatalog.domain.product.decorator;

import com.acme.productcatalog.domain.product.ProductKind;

import java.util.Map;
import java.util.Set;

public record BundleCompositionPolicies(Map<ProductKind, Set<ProductKind>> compositionPolicies) {

    public boolean allowsBundleItemKind(ProductKind productKind, ProductKind itemKind) {
        return compositionPolicies.getOrDefault(productKind, Set.of()).contains(itemKind);
    }

}
