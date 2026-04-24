package com.acme.orderquestionnaire.adapters.out.api.distribution.channel.dto;

import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("ChannelDistributionApi DTOs")
class ChannelDistributionApiDtoTest {

    @Test
    @DisplayName("data dto should expose all values")
    void shouldExposeDataValues() {
        ChannelDistributionApiDataDto dto = new ChannelDistributionApiDataDto("id", "ref", "name", true);

        assertEquals("id", dto.id());
        assertEquals("ref", dto.referenceCode());
        assertEquals("name", dto.name());
        assertTrue(dto.active());
    }

    @Test
    @DisplayName("response dto should expose wrapped data")
    void shouldExposeResponseData() {
        ChannelDistributionApiDataDto data = new ChannelDistributionApiDataDto("id", "ref", "name", false);
        ChannelDistributionApiResponseDto response = new ChannelDistributionApiResponseDto(data);

        assertEquals(data, response.data());
    }
}

