package com.acme.orderquestionnaire.adapters.out.mongo.channel;

import com.acme.orderquestionnaire.adapters.out.mongo.channel.repository.ChannelDistributionRepository;
import com.acme.orderquestionnaire.application.channel.port.out.ChannelDistributionOutPort;
import com.acme.shared.stereotypes.adapter.OutputAdapter;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@OutputAdapter
public class ChannelDistributionAdapter implements ChannelDistributionOutPort {

    private final ChannelDistributionRepository channelDistributionRepository;

    public ChannelDistributionAdapter(ChannelDistributionRepository channelDistributionRepository) {
        this.channelDistributionRepository = Objects.requireNonNull(channelDistributionRepository,
                "channelDistributionRepository must not be null");
    }

    @Override
    public boolean existsById(String channelDistributionId) {
        return channelDistributionRepository.existsById(channelDistributionId);
    }
}

