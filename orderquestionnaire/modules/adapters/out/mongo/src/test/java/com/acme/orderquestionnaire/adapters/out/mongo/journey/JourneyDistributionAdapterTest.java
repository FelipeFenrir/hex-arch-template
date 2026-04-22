package com.acme.orderquestionnaire.adapters.out.mongo.journey;

import com.acme.orderquestionnaire.adapters.out.mongo.journey.repository.JourneyDistributionRepository;
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
@DisplayName("JourneyDistributionAdapter")
class JourneyDistributionAdapterTest {

    @Mock
    private JourneyDistributionRepository repository;

    @InjectMocks
    private JourneyDistributionAdapter adapter;

    @Test
    @DisplayName("existsById should return true when journey distribution exists")
    void shouldReturnTrueWhenJourneyExists() {
        when(repository.existsById("journey_satisfaction")).thenReturn(true);

        boolean result = adapter.existsById("journey_satisfaction");

        assertTrue(result);
        verify(repository).existsById("journey_satisfaction");
    }

    @Test
    @DisplayName("existsById should return false when journey distribution does not exist")
    void shouldReturnFalseWhenJourneyNotFound() {
        when(repository.existsById("journey_unknown")).thenReturn(false);

        boolean result = adapter.existsById("journey_unknown");

        assertFalse(result);
        verify(repository).existsById("journey_unknown");
    }

    @Test
    @DisplayName("constructor should reject null repository")
    void shouldRejectNullRepository() {
        assertThrows(NullPointerException.class, () -> new JourneyDistributionAdapter(null));
    }
}

