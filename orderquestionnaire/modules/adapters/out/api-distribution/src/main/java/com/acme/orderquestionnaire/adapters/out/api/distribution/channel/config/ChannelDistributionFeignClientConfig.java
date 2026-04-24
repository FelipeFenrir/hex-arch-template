package com.acme.orderquestionnaire.adapters.out.api.distribution.channel.config;

import feign.Request;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class ChannelDistributionFeignClientConfig {

    @Bean
    public Request.Options channelDistributionRequestOptions(
            @Value("${orderquestionnaire.adapters.out.channel-distribution.integrations.api.connect-timeout:2s}") String connectTimeout,
            @Value("${orderquestionnaire.adapters.out.channel-distribution.integrations.api.read-timeout:3s}") String readTimeout
    ) {
        Duration connect = DurationStyle.detectAndParse(connectTimeout);
        Duration read = DurationStyle.detectAndParse(readTimeout);

        return new Request.Options(
                connect.toMillis(),
                TimeUnit.MILLISECONDS,
                read.toMillis(),
                TimeUnit.MILLISECONDS,
                true
        );
    }

    @Bean
    public RequestInterceptor channelDistributionRequestInterceptor(
            @Value("${orderquestionnaire.adapters.out.channel-distribution.integrations.api.authentication:false}") boolean authentication
    ) {
        return new ChannelDistributionRequestInterceptor(authentication);
    }
}


