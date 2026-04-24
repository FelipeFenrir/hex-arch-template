package com.acme.orderquestionnaire.config.adapter.out.distribution.journey;

import com.acme.orderquestionnaire.application.journey.port.out.JourneyDistributionOutPort;
import com.acme.orderquestionnaire.config.adapter.out.distribution.DistributionAdapterProperties;
import com.acme.orderquestionnaire.config.adapter.out.distribution.DistributionAdapterStrategy;
import com.acme.orderquestionnaire.config.adapter.out.distribution.DistributionProvider;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@DisplayName("JourneyDistributionOutPortDelegate")
class JourneyDistributionOutPortDelegateTest {

    @Test
    @DisplayName("existsById should route to configured provider strategy")
    void shouldRouteToConfiguredProvider() {
        JourneyDistributionOutPort mongoAdapter = mock(JourneyDistributionOutPort.class);
        JourneyDistributionOutPort apiAdapter = mock(JourneyDistributionOutPort.class);
        when(mongoAdapter.existsById("journey_01")).thenReturn(true);
        when(apiAdapter.existsById("journey_01")).thenReturn(false);

        DistributionAdapterProperties properties = new DistributionAdapterProperties();
        properties.setProvider(DistributionProvider.MONGO);

        JourneyDistributionOutPortDelegate delegate = new JourneyDistributionOutPortDelegate(
                properties,
                List.of(
                        new DistributionAdapterStrategy<>(DistributionProvider.MONGO, mongoAdapter),
                        new DistributionAdapterStrategy<>(DistributionProvider.API, apiAdapter)
                )
        );

        assertTrue(delegate.existsById("journey_01"));

        properties.setProvider(DistributionProvider.API);
        assertFalse(delegate.existsById("journey_01"));
    }

    @Test
    @DisplayName("existsById should fail when configured provider has no strategy")
    void shouldFailWhenProviderStrategyIsMissing() {
        JourneyDistributionOutPort mongoAdapter = mock(JourneyDistributionOutPort.class);

        DistributionAdapterProperties properties = new DistributionAdapterProperties();
        properties.setProvider(DistributionProvider.API);

        JourneyDistributionOutPortDelegate delegate = new JourneyDistributionOutPortDelegate(
                properties,
                List.of(new DistributionAdapterStrategy<>(DistributionProvider.MONGO, mongoAdapter))
        );

        assertThrows(IllegalStateException.class, () -> delegate.existsById("journey_01"));
    }

    @Test
    @DisplayName("constructor should reject null arguments")
    void shouldRejectNullArguments() {
        assertThrows(NullPointerException.class, () -> new JourneyDistributionOutPortDelegate(null, List.of()));
        assertThrows(NullPointerException.class,
                () -> new JourneyDistributionOutPortDelegate(new DistributionAdapterProperties(), null));
    }
}

