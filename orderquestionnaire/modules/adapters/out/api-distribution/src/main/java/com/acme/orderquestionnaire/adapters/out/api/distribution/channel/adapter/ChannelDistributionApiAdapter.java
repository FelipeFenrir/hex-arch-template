package com.acme.orderquestionnaire.adapters.out.api.distribution.channel.adapter;

import com.acme.orderquestionnaire.adapters.out.api.distribution.channel.client.ChannelDistributionApiClient;
import com.acme.orderquestionnaire.adapters.out.api.distribution.channel.dto.ChannelDistributionApiDataDto;
import com.acme.orderquestionnaire.adapters.out.api.distribution.channel.dto.ChannelDistributionApiResponseDto;
import com.acme.orderquestionnaire.adapters.out.api.distribution.channel.error.ChannelDistributionApiErrorTranslator;
import com.acme.orderquestionnaire.application.channel.port.out.ChannelDistributionOutPort;
import com.acme.shared.stereotypes.adapter.OutputAdapter;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("channelDistributionApiAdapter")
@OutputAdapter
public class ChannelDistributionApiAdapter implements ChannelDistributionOutPort {

    private final ChannelDistributionApiClient channelDistributionApiClient;
    private final ChannelDistributionApiErrorTranslator errorTranslator;

    public ChannelDistributionApiAdapter(
            ChannelDistributionApiClient channelDistributionApiClient,
            ChannelDistributionApiErrorTranslator errorTranslator
    ) {
        this.channelDistributionApiClient = Objects.requireNonNull(channelDistributionApiClient,
                "channelDistributionApiClient must not be null");
        this.errorTranslator = Objects.requireNonNull(errorTranslator, "errorTranslator must not be null");
    }

    @Override
    public boolean existsById(String channelDistributionId) {
        try {
            ChannelDistributionApiResponseDto response = channelDistributionApiClient.getById(channelDistributionId);
            ChannelDistributionApiDataDto data = response == null ? null : response.data();
            return data != null && channelDistributionId.equals(data.id());
        } catch (Exception ex) {
            return errorTranslator.translateExistsByIdFailure(ex, channelDistributionId);
        }
    }
}

