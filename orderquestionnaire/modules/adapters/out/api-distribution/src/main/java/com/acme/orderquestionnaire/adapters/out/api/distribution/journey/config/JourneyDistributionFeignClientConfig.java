package com.acme.orderquestionnaire.adapters.out.api.distribution.journey.config;

import feign.Request;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class JourneyDistributionFeignClientConfig {

    @Bean
    public Request.Options journeyDistributionRequestOptions(
            @Value("${orderquestionnaire.adapters.out.journey-distribution.integrations.api.connect-timeout:2s}") String connectTimeout,
            @Value("${orderquestionnaire.adapters.out.journey-distribution.integrations.api.read-timeout:3s}") String readTimeout
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
    public RequestInterceptor journeyDistributionRequestInterceptor(
            @Value("${orderquestionnaire.adapters.out.journey-distribution.integrations.api.authentication:false}") boolean authentication
    ) {
        return new JourneyDistributionRequestInterceptor(authentication);
    }
}


