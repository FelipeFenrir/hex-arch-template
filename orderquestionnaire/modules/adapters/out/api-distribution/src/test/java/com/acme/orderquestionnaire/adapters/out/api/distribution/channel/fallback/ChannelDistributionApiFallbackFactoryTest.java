package com.acme.orderquestionnaire.adapters.out.api.distribution.channel.fallback;

import com.acme.orderquestionnaire.adapters.out.api.distribution.channel.client.ChannelDistributionApiClient;
import com.acme.orderquestionnaire.adapters.out.api.distribution.channel.dto.ChannelDistributionApiResponseDto;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@UnitTest
@DisplayName("ChannelDistributionApiFallbackFactory")
class ChannelDistributionApiFallbackFactoryTest {

    @Test
    @DisplayName("create should return a client fallback that responds with null data")
    void shouldCreateFallbackClient() {
        ChannelDistributionApiFallbackFactory factory = new ChannelDistributionApiFallbackFactory();

        ChannelDistributionApiClient fallback = factory.create(new RuntimeException("downstream"));
        ChannelDistributionApiResponseDto response = fallback.getById("channel_01");

        assertNotNull(fallback);
        assertNotNull(response);
        assertNull(response.data());
    }
}

