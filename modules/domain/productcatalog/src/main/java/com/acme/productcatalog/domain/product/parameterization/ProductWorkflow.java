package com.acme.productcatalog.domain.product.parameterization;

import com.acme.productcatalog.domain.common.vo.TransitionStatus;
import com.acme.productcatalog.domain.exception.TransitionStatusNotFoundException;

import java.util.List;

public final class ProductWorkflow {
    private final List<TransitionStatus> productTransitionSatus;

    private ProductWorkflow(final List<TransitionStatus> productTransitionSatus) {
        this.productTransitionSatus = productTransitionSatus;
    }

    public TransitionStatus getTransition(ProductStatus onStatus, ProductStatus nextStatus) {
        return this.productTransitionSatus
                .stream()
                .filter(
                        transitionStatus -> transitionStatus.origin().equals(onStatus) &&
                                transitionStatus.destination().equals(nextStatus)
                )
                .findFirst()
                .orElseThrow(() -> new TransitionStatusNotFoundException(
                        "Invalid Transition of " + onStatus + " to " + nextStatus));
    }
}
