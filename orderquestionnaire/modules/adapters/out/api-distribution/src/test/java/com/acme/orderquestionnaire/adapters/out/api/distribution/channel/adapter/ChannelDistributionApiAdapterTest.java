package com.acme.orderquestionnaire.adapters.out.api.distribution.channel.adapter;

import com.acme.orderquestionnaire.adapters.out.api.distribution.channel.client.ChannelDistributionApiClient;
import com.acme.orderquestionnaire.adapters.out.api.distribution.channel.dto.ChannelDistributionApiDataDto;
import com.acme.orderquestionnaire.adapters.out.api.distribution.channel.dto.ChannelDistributionApiResponseDto;
import com.acme.orderquestionnaire.adapters.out.api.distribution.channel.error.ChannelDistributionApiErrorTranslator;
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
@DisplayName("ChannelDistributionApiAdapter")
class ChannelDistributionApiAdapterTest {

    @Mock
    private ChannelDistributionApiClient client;

    @Mock
    private ChannelDistributionApiErrorTranslator translator;

    @InjectMocks
    private ChannelDistributionApiAdapter adapter;

    @Test
    @DisplayName("existsById should return true when API returns matching id")
    void shouldReturnTrueWhenApiReturnsMatchingId() {
        when(client.getById("channel_01"))
                .thenReturn(new ChannelDistributionApiResponseDto(
                        new ChannelDistributionApiDataDto("channel_01", "channel_01", "Mobile", true)));

        boolean result = adapter.existsById("channel_01");

        assertTrue(result);
        verify(client).getById("channel_01");
    }

    @Test
    @DisplayName("existsById should return false when API returns null response")
    void shouldReturnFalseWhenApiReturnsNullResponse() {
        when(client.getById("channel_01")).thenReturn(null);

        boolean result = adapter.existsById("channel_01");

        assertFalse(result);
    }

    @Test
    @DisplayName("existsById should return false when API id does not match")
    void shouldReturnFalseWhenApiReturnsDifferentId() {
        when(client.getById("channel_01"))
                .thenReturn(new ChannelDistributionApiResponseDto(
                        new ChannelDistributionApiDataDto("channel_x", "channel_x", "Web", true)));

        boolean result = adapter.existsById("channel_01");

        assertFalse(result);
    }

    @Test
    @DisplayName("existsById should delegate failure translation when client throws")
    void shouldTranslateFailures() {
        RuntimeException error = new RuntimeException("downstream");
        when(client.getById("channel_01")).thenThrow(error);
        when(translator.translateExistsByIdFailure(error, "channel_01")).thenReturn(true);

        boolean result = adapter.existsById("channel_01");

        assertTrue(result);
        verify(translator).translateExistsByIdFailure(error, "channel_01");
    }

    @Test
    @DisplayName("constructor should reject null dependencies")
    void shouldRejectNullDependencies() {
        assertThrows(NullPointerException.class, () -> new ChannelDistributionApiAdapter(null, translator));
        assertThrows(NullPointerException.class, () -> new ChannelDistributionApiAdapter(client, null));
    }
}

