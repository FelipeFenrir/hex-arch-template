package com.acme.orderquestionnaire.adapters.out.api.distribution.channel.config;

import com.acme.shared.stereotypes.test.UnitTest;
import feign.Request;
import feign.RequestInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@UnitTest
@DisplayName("ChannelDistributionFeignClientConfig")
class ChannelDistributionFeignClientConfigTest {

    @Test
    @DisplayName("request options should be created from configured durations")
    void shouldCreateRequestOptions() {
        ChannelDistributionFeignClientConfig config = new ChannelDistributionFeignClientConfig();

        Request.Options options = config.channelDistributionRequestOptions("2s", "3s");

        assertNotNull(options);
    }

    @Test
    @DisplayName("request interceptor should be created")
    void shouldCreateRequestInterceptor() {
        ChannelDistributionFeignClientConfig config = new ChannelDistributionFeignClientConfig();

        RequestInterceptor interceptor = config.channelDistributionRequestInterceptor(true);

        assertNotNull(interceptor);
    }
}


