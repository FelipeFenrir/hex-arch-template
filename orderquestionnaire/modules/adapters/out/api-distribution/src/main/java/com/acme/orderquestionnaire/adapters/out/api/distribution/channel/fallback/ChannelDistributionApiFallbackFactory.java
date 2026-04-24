package com.acme.orderquestionnaire.adapters.out.api.distribution.channel.fallback;

import com.acme.orderquestionnaire.adapters.out.api.distribution.channel.client.ChannelDistributionApiClient;
import com.acme.orderquestionnaire.adapters.out.api.distribution.channel.dto.ChannelDistributionApiResponseDto;
import org.springframework.stereotype.Component;
import org.springframework.cloud.openfeign.FallbackFactory;

@Component
public class ChannelDistributionApiFallbackFactory implements FallbackFactory<ChannelDistributionApiClient> {

    @Override
    public ChannelDistributionApiClient create(Throwable cause) {
        return channelId -> new ChannelDistributionApiResponseDto(null);
    }
}


