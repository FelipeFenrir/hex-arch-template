package com.acme.orderquestionnaire.adapters.out.api.distribution.unit.journey.config;

import com.acme.orderquestionnaire.adapters.out.api.distribution.journey.config.JourneyDistributionFeignClientConfig;
import com.acme.shared.stereotypes.test.UnitTest;
import feign.Request;
import feign.RequestInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@UnitTest
@DisplayName("JourneyDistributionFeignClientConfig")
class JourneyDistributionFeignClientConfigTest {

    @Test
    @DisplayName("request options should be created from configured durations")
    void shouldCreateRequestOptions() {
        JourneyDistributionFeignClientConfig config = new JourneyDistributionFeignClientConfig();

        Request.Options options = config.journeyDistributionRequestOptions("2s", "3s");

        assertNotNull(options);
    }

    @Test
    @DisplayName("request interceptor should be created")
    void shouldCreateRequestInterceptor() {
        JourneyDistributionFeignClientConfig config = new JourneyDistributionFeignClientConfig();

        RequestInterceptor interceptor = config.journeyDistributionRequestInterceptor(true);

        assertNotNull(interceptor);
    }
}


