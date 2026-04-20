package com.acme.productcatalog.domain.offer;

import com.acme.productcatalog.domain.product.parameterization.ProductStatus;

public interface Offer {
    void setId();
    void setId(String id);
    Id getId();
    String getSourceId();
    String getName();
    String getSurname();
    String getDescription();
    ProductStatus getStatus();
}
