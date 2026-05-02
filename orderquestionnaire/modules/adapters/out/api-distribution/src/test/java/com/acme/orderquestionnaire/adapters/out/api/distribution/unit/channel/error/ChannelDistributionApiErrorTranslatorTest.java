package com.acme.orderquestionnaire.adapters.out.api.distribution.unit.channel.error;

import com.acme.orderquestionnaire.adapters.out.api.distribution.channel.error.ChannelDistributionApiErrorTranslator;
import com.acme.shared.stereotypes.test.UnitTest;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
@DisplayName("ChannelDistributionApiErrorTranslator")
class ChannelDistributionApiErrorTranslatorTest {

    private final ChannelDistributionApiErrorTranslator translator = new ChannelDistributionApiErrorTranslator();

    @Test
    @DisplayName("should return false for 404 not found")
    void shouldReturnFalseForNotFound() {
        FeignException exception = buildNotFound();

        boolean result = translator.translateExistsByIdFailure(exception, "channel_01");

        assertFalse(result);
    }

    @Test
    @DisplayName("should return false for generic failures")
    void shouldReturnFalseForGenericFailure() {
        boolean result = translator.translateExistsByIdFailure(new RuntimeException("boom"), "channel_01");

        assertFalse(result);
    }

    @Test
    @DisplayName("should reject null channel id")
    void shouldRejectNullChannelId() {
        assertThrows(NullPointerException.class,
                () -> translator.translateExistsByIdFailure(new RuntimeException("boom"), null));
    }

    private FeignException buildNotFound() {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "http://localhost/distribution/channel/channel_01",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                null
        );

        Response response = Response.builder()
                .status(404)
                .reason("Not Found")
                .request(request)
                .headers(Map.of())
                .build();

        return FeignException.errorStatus("ChannelDistributionApiClient#getById", response);
    }
}

