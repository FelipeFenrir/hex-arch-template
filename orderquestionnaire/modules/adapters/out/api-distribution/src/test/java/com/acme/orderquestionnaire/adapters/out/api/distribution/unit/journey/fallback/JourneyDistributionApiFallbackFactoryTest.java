package com.acme.orderquestionnaire.adapters.out.api.distribution.unit.journey.fallback;

import com.acme.orderquestionnaire.adapters.out.api.distribution.journey.client.JourneyDistributionApiClient;
import com.acme.orderquestionnaire.adapters.out.api.distribution.journey.dto.JourneyDistributionApiResponseDto;
import com.acme.orderquestionnaire.adapters.out.api.distribution.journey.fallback.JourneyDistributionApiFallbackFactory;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@UnitTest
@DisplayName("JourneyDistributionApiFallbackFactory")
class JourneyDistributionApiFallbackFactoryTest {

    @Test
    @DisplayName("create should return a client fallback that responds with null data")
    void shouldCreateFallbackClient() {
        JourneyDistributionApiFallbackFactory factory = new JourneyDistributionApiFallbackFactory();

        JourneyDistributionApiClient fallback = factory.create(new RuntimeException("downstream"));
        JourneyDistributionApiResponseDto response = fallback.getById("journey_01");

        assertNotNull(fallback);
        assertNotNull(response);
        assertNull(response.data());
    }
}

