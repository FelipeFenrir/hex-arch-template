package com.acme.orderquestionnaire.adapters.out.mongo.channel;

import com.acme.orderquestionnaire.adapters.out.mongo.channel.repository.ChannelDistributionRepository;
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
@DisplayName("ChannelDistributionAdapter")
class ChannelDistributionAdapterTest {

    @Mock
    private ChannelDistributionRepository repository;

    @InjectMocks
    private ChannelDistributionAdapter adapter;

    @Test
    @DisplayName("existsById should return true when channel distribution exists")
    void shouldReturnTrueWhenChannelExists() {
        when(repository.existsById("channel_mobile")).thenReturn(true);

        boolean result = adapter.existsById("channel_mobile");

        assertTrue(result);
        verify(repository).existsById("channel_mobile");
    }

    @Test
    @DisplayName("existsById should return false when channel distribution does not exist")
    void shouldReturnFalseWhenChannelNotFound() {
        when(repository.existsById("channel_unknown")).thenReturn(false);

        boolean result = adapter.existsById("channel_unknown");

        assertFalse(result);
        verify(repository).existsById("channel_unknown");
    }

    @Test
    @DisplayName("constructor should reject null repository")
    void shouldRejectNullRepository() {
        assertThrows(NullPointerException.class, () -> new ChannelDistributionAdapter(null));
    }
}

