package com.acme.productcatalog.domain.product.decorator;

import com.acme.productcatalog.domain.exception.EmptyBundleProductException;
import com.acme.productcatalog.domain.exception.ProductCompositionPolicyViolatedException;
import com.acme.productcatalog.domain.product.ProductKind;

import java.util.List;

public final class BundleCapability implements ProductCapability {
    private final List<BundleItem> bundleItems;

    private BundleCapability(final List<BundleItem> bundleItems) {
        this.bundleItems = bundleItems;
        //this.pricingRule = pricingRule;
        //this.overridePrice = overridePrice;
        //this.itemPriceOverrides = Map.copyOf(itemPriceOverrides);
    }

    private static void validateItemKind(final ProductKind productKind, final BundleItem bundleItem,
                                         final BundleCompositionPolicies compositionPolicies) {
        if (!compositionPolicies.allowsBundleItemKind(productKind, bundleItem.productKind())) {
            throw new ProductCompositionPolicyViolatedException(
                    "Products of kind " + productKind +
                            " cannot contain bundle items from kind " + bundleItem.productKind()
            );
        }
    }
    private static void validateItemKind(final ProductKind productKind, final List<BundleItem> bundleItems,
                                         final BundleCompositionPolicies compositionPolicies) {
        isEmptyBundle(bundleItems);
        for (BundleItem item : bundleItems) {
            validateItemKind(productKind, item, compositionPolicies);
        }
    }
    private static void isEmptyBundle(List<BundleItem> bundleItems) {
        if (bundleItems == null || bundleItems.isEmpty())
            throw new EmptyBundleProductException("Bundle must have items");
    }

    public static BundleCapability createNew(final ProductKind productKind, final List<BundleItem> bundleItems,
                                             final BundleCompositionPolicies compositionPolicies) {
        validateItemKind(productKind, bundleItems, compositionPolicies);
        return new BundleCapability(bundleItems);
    }

    public static BundleCapability rehydrate(final List<BundleItem> bundleItems) {
        isEmptyBundle(bundleItems);
        return new BundleCapability(bundleItems);
    }

    public void addBundleItem(final ProductKind productKind, final BundleItem bundleItem,
                              final BundleCompositionPolicies compositionPolicies) {
        validateItemKind(productKind, bundleItem, compositionPolicies);
        this.bundleItems.add(bundleItem);
    }

    public void removeBundleItem(final BundleItem bundleItem) {
        isEmptyBundle(this.bundleItems);
        //if(this.bundleItems.size() > 1)
            this.bundleItems.removeIf(item -> item.productId().equals(bundleItem.productId()));
    }

    public List<BundleItem> bundleItems() { return this.bundleItems; }
}
