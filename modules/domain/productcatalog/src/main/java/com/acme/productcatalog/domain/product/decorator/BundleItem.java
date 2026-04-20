package com.acme.productcatalog.domain.product.decorator;

import com.acme.productcatalog.domain.exception.BundleItemInvalidConfigurationException;
import com.acme.productcatalog.domain.product.ProductKind;
import com.acme.shared.vo.Id;

public class BundleItem  {

    private final Id productId;
    private final ProductKind productKind;
    private boolean isMandatory;
    private int minimumQuantity;
    private int maximumQuantity;

    private BundleItem(Id productId, ProductKind productKind, boolean isMandatory,
                      int minimumQuantity, int maximumQuantity) {
        this.cardinalityValidation(minimumQuantity, maximumQuantity);
        this.productId = productId;
        this.productKind = productKind;
        this.isMandatory = isMandatory;
        this.minimumQuantity = minimumQuantity;
        this.maximumQuantity = maximumQuantity;
    }

    private void cardinalityValidation(final int minimumQuantity, final int maximumQuantity) {
        if(minimumQuantity < 0)
            throw new BundleItemInvalidConfigurationException("Invalid cardinality of the bundle item: " +
                    "the minimum quantity is 0 or less");

        if(maximumQuantity < 1)
            throw new BundleItemInvalidConfigurationException("Invalid cardinality of the bundle item: " +
                    "the maximum quantity is less than 1");

        if (minimumQuantity > maximumQuantity)
            throw new BundleItemInvalidConfigurationException("Invalid cardinality of the bundle item: " +
                    "the minimum quantity is greater than the maximum quantity");
    }

    public static BundleItem createNew(final ProductKind productKind, final boolean isMandatory,
                                       final int minimumQuantity, final int maximumQuantity) {
        return new BundleItem(Id.withoutId(), productKind, isMandatory, minimumQuantity, maximumQuantity);
    }
    public static BundleItem rehydrate(final Id productId, final ProductKind productKind, final boolean isMandatory,
                                        final int minimumQuantity, final int maximumQuantity) {
        return new BundleItem(productId, productKind, isMandatory, minimumQuantity, maximumQuantity);
    }
    public static BundleItem rehydrate(final String productId, final ProductKind productKind, final boolean isMandatory,
                                        final int minimumQuantity, final int maximumQuantity) {
        return rehydrate(Id.withId(productId), productKind, isMandatory, minimumQuantity, maximumQuantity);
    }

    public void adjustCardinality(final int minimumQuantity, final int maximumQuantity) {
        cardinalityValidation(minimumQuantity, maximumQuantity);
        this.minimumQuantity = minimumQuantity;
        this.maximumQuantity = maximumQuantity;
    }
    public void isMandatory(final boolean isMandatory) {
        this.isMandatory = isMandatory;
    }

    public Id productId() {
        return productId;
    }
    public ProductKind productKind() {
        return productKind;
    }
    public boolean isMandatory() {
        return isMandatory;
    }
    public int minimumQuantity() {
        return minimumQuantity;
    }
    public int maximumQuantity() {
        return maximumQuantity;
    }
}