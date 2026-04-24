package com.acme.orderquestionnaire.adapters.out.api.distribution.channel.client;

import com.acme.orderquestionnaire.adapters.out.api.distribution.channel.config.ChannelDistributionFeignClientConfig;
import com.acme.orderquestionnaire.adapters.out.api.distribution.channel.dto.ChannelDistributionApiResponseDto;
import com.acme.orderquestionnaire.adapters.out.api.distribution.channel.fallback.ChannelDistributionApiFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "channelDistributionApiClient",
        url = "${orderquestionnaire.adapters.out.channel-distribution.integrations.api.base-url}",
        configuration = ChannelDistributionFeignClientConfig.class,
        fallbackFactory = ChannelDistributionApiFallbackFactory.class
)
public interface ChannelDistributionApiClient {

    @GetMapping("/distribution/channel/{channelId}")
    ChannelDistributionApiResponseDto getById(@PathVariable("channelId") String channelId);
}



