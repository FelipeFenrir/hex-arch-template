package com.acme.orderquestionnaire.config.adapter.out.distribution.journey;

import com.acme.orderquestionnaire.application.journey.port.out.JourneyDistributionOutPort;
import com.acme.orderquestionnaire.config.adapter.out.distribution.DistributionAdapterProperties;
import com.acme.orderquestionnaire.config.adapter.out.distribution.DistributionAdapterStrategy;
import com.acme.orderquestionnaire.config.adapter.out.distribution.DistributionProvider;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class JourneyDistributionOutPortDelegate implements JourneyDistributionOutPort {

    private final DistributionAdapterProperties properties;
    private final Map<DistributionProvider, JourneyDistributionOutPort> strategies;

    public JourneyDistributionOutPortDelegate(
            DistributionAdapterProperties properties,
            List<DistributionAdapterStrategy<JourneyDistributionOutPort>> strategies
    ) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.strategies = Objects.requireNonNull(strategies, "strategies must not be null")
                .stream()
                .collect(Collectors.toMap(
                        DistributionAdapterStrategy::provider,
                        DistributionAdapterStrategy::adapter,
                        (left, right) -> right
                ));
    }

    @Override
    public boolean existsById(String journeyDistributionId) {
        return resolveAdapter().existsById(journeyDistributionId);
    }

    private JourneyDistributionOutPort resolveAdapter() {
        return java.util.Optional.ofNullable(strategies.get(properties.getProvider()))
                .orElseThrow(() -> new IllegalStateException(
                        "No JourneyDistributionOutPort strategy configured for provider [%s]".formatted(properties.getProvider())
                ));
    }
}

