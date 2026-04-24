package com.acme.orderquestionnaire.config.adapter.out.distribution.channel;

import com.acme.orderquestionnaire.application.channel.port.out.ChannelDistributionOutPort;
import com.acme.orderquestionnaire.config.adapter.out.distribution.DistributionAdapterProperties;
import com.acme.orderquestionnaire.config.adapter.out.distribution.DistributionAdapterStrategy;
import com.acme.orderquestionnaire.config.adapter.out.distribution.DistributionProvider;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ChannelDistributionOutPortDelegate implements ChannelDistributionOutPort {

    private final DistributionAdapterProperties properties;
    private final Map<DistributionProvider, ChannelDistributionOutPort> strategies;

    public ChannelDistributionOutPortDelegate(
            DistributionAdapterProperties properties,
            List<DistributionAdapterStrategy<ChannelDistributionOutPort>> strategies
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
    public boolean existsById(String channelDistributionId) {
        return resolveAdapter().existsById(channelDistributionId);
    }

    private ChannelDistributionOutPort resolveAdapter() {
        return java.util.Optional.ofNullable(strategies.get(properties.getProvider()))
                .orElseThrow(() -> new IllegalStateException(
                        "No ChannelDistributionOutPort strategy configured for provider [%s]".formatted(properties.getProvider())
                ));
    }
}


