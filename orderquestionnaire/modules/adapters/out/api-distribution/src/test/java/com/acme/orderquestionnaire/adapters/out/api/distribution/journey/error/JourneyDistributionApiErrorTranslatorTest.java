package com.acme.orderquestionnaire.adapters.out.api.distribution.journey.error;

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
@DisplayName("JourneyDistributionApiErrorTranslator")
class JourneyDistributionApiErrorTranslatorTest {

    private final JourneyDistributionApiErrorTranslator translator = new JourneyDistributionApiErrorTranslator();

    @Test
    @DisplayName("should return false for 404 not found")
    void shouldReturnFalseForNotFound() {
        FeignException exception = buildNotFound();

        boolean result = translator.translateExistsByIdFailure(exception, "journey_01");

        assertFalse(result);
    }

    @Test
    @DisplayName("should return false for generic failures")
    void shouldReturnFalseForGenericFailure() {
        boolean result = translator.translateExistsByIdFailure(new RuntimeException("boom"), "journey_01");

        assertFalse(result);
    }

    @Test
    @DisplayName("should reject null journey id")
    void shouldRejectNullJourneyId() {
        assertThrows(NullPointerException.class,
                () -> translator.translateExistsByIdFailure(new RuntimeException("boom"), null));
    }

    private FeignException buildNotFound() {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "http://localhost/distribution/journey/journey_01",
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

        return FeignException.errorStatus("JourneyDistributionApiClient#getById", response);
    }
}

