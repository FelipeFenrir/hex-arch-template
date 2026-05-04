package com.acme.orderquestionnaire.adapters.out.api.distribution.unit.journey.dto;

import com.acme.orderquestionnaire.adapters.out.api.distribution.journey.dto.JourneyDistributionApiDataDto;
import com.acme.orderquestionnaire.adapters.out.api.distribution.journey.dto.JourneyDistributionApiResponseDto;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("JourneyDistributionApi DTOs")
class JourneyDistributionApiDtoTest {

    @Test
    @DisplayName("data dto should expose all values")
    void shouldExposeDataValues() {
        JourneyDistributionApiDataDto dto = new JourneyDistributionApiDataDto("id", "ref", "name", true);

        assertEquals("id", dto.id());
        assertEquals("ref", dto.referenceCode());
        assertEquals("name", dto.name());
        assertTrue(dto.active());
    }

    @Test
    @DisplayName("response dto should expose wrapped data")
    void shouldExposeResponseData() {
        JourneyDistributionApiDataDto data = new JourneyDistributionApiDataDto("id", "ref", "name", false);
        JourneyDistributionApiResponseDto response = new JourneyDistributionApiResponseDto(data);

        assertEquals(data, response.data());
    }
}

