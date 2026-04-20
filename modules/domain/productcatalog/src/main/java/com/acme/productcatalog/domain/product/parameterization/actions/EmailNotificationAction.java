package com.acme.productcatalog.domain.product.parameterization.actions;

import com.acme.productcatalog.domain.product.Product;

public interface EmailNotificationAction {
    void sendNotification(Product product);
}
