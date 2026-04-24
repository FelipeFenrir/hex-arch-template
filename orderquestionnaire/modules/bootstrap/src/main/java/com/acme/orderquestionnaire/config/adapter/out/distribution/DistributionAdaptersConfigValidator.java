package com.acme.orderquestionnaire.config.adapter.out.distribution;

import jakarta.annotation.PostConstruct;

import java.util.Objects;

public class DistributionAdaptersConfigValidator {

    private final OrderQuestionnaireOutAdaptersProperties properties;

    public DistributionAdaptersConfigValidator(OrderQuestionnaireOutAdaptersProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
    }

    @PostConstruct
    public void validate() {
        validate("channel-distribution", properties.getChannelDistribution());
        validate("journey-distribution", properties.getJourneyDistribution());
    }

    private void validate(String adapterName, DistributionAdapterProperties properties) {
        Objects.requireNonNull(properties, () -> adapterName + " properties must not be null");
        DistributionProvider provider = Objects.requireNonNull(properties.getProvider(),
                () -> adapterName + " provider must not be null");

        if (!provider.isEnabled()) {
            throw new IllegalStateException("Provider [%s] is not enabled yet for [%s]".formatted(provider, adapterName));
        }

        if (provider == DistributionProvider.API) {
            ApiIntegrationProperties api = Objects.requireNonNull(properties.getIntegrations(),
                            () -> adapterName + " integrations must not be null")
                    .getApi();

            if (api == null || api.getBaseUrl() == null || api.getBaseUrl().isBlank()) {
                throw new IllegalStateException("API base-url must be configured for [%s]".formatted(adapterName));
            }
        }
    }
}

