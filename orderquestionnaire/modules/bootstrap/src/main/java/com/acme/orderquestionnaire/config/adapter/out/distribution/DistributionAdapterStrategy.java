package com.acme.orderquestionnaire.config.adapter.out.distribution;

public record DistributionAdapterStrategy<T>(
        DistributionProvider provider,
        T adapter
) {
}

