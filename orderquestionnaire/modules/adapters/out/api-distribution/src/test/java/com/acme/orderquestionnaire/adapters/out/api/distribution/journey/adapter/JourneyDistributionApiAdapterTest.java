package com.acme.orderquestionnaire.adapters.out.api.distribution.journey.adapter;

import com.acme.orderquestionnaire.adapters.out.api.distribution.journey.client.JourneyDistributionApiClient;
import com.acme.orderquestionnaire.adapters.out.api.distribution.journey.dto.JourneyDistributionApiDataDto;
import com.acme.orderquestionnaire.adapters.out.api.distribution.journey.dto.JourneyDistributionApiResponseDto;
import com.acme.orderquestionnaire.adapters.out.api.distribution.journey.error.JourneyDistributionApiErrorTranslator;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("JourneyDistributionApiAdapter")
class JourneyDistributionApiAdapterTest {

    @Mock
    private JourneyDistributionApiClient client;

    @Mock
    private JourneyDistributionApiErrorTranslator translator;

    @InjectMocks
    private JourneyDistributionApiAdapter adapter;

    @Test
    @DisplayName("existsById should return true when API returns matching id")
    void shouldReturnTrueWhenApiReturnsMatchingId() {
        when(client.getById("journey_01"))
                .thenReturn(new JourneyDistributionApiResponseDto(
                        new JourneyDistributionApiDataDto("journey_01", "journey_01", "Checkout", true)));

        boolean result = adapter.existsById("journey_01");

        assertTrue(result);
        verify(client).getById("journey_01");
    }

    @Test
    @DisplayName("existsById should return false when API returns null response")
    void shouldReturnFalseWhenApiReturnsNullResponse() {
        when(client.getById("journey_01")).thenReturn(null);

        boolean result = adapter.existsById("journey_01");

        assertFalse(result);
    }

    @Test
    @DisplayName("existsById should return false when API id does not match")
    void shouldReturnFalseWhenApiReturnsDifferentId() {
        when(client.getById("journey_01"))
                .thenReturn(new JourneyDistributionApiResponseDto(
                        new JourneyDistributionApiDataDto("journey_x", "journey_x", "Support", true)));

        boolean result = adapter.existsById("journey_01");

        assertFalse(result);
    }

    @Test
    @DisplayName("existsById should delegate failure translation when client throws")
    void shouldTranslateFailures() {
        RuntimeException error = new RuntimeException("downstream");
        when(client.getById("journey_01")).thenThrow(error);
        when(translator.translateExistsByIdFailure(error, "journey_01")).thenReturn(true);

        boolean result = adapter.existsById("journey_01");

        assertTrue(result);
        verify(translator).translateExistsByIdFailure(error, "journey_01");
    }

    @Test
    @DisplayName("constructor should reject null dependencies")
    void shouldRejectNullDependencies() {
        assertThrows(NullPointerException.class, () -> new JourneyDistributionApiAdapter(null, translator));
        assertThrows(NullPointerException.class, () -> new JourneyDistributionApiAdapter(client, null));
    }
}

