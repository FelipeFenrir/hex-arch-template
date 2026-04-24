package com.acme.orderquestionnaire.config.adapter.out.distribution.channel;

import com.acme.orderquestionnaire.application.channel.port.out.ChannelDistributionOutPort;
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
@DisplayName("ChannelDistributionOutPortDelegate")
class ChannelDistributionOutPortDelegateTest {

    @Test
    @DisplayName("existsById should route to configured provider strategy")
    void shouldRouteToConfiguredProvider() {
        ChannelDistributionOutPort mongoAdapter = mock(ChannelDistributionOutPort.class);
        ChannelDistributionOutPort apiAdapter = mock(ChannelDistributionOutPort.class);
        when(mongoAdapter.existsById("channel_01")).thenReturn(true);
        when(apiAdapter.existsById("channel_01")).thenReturn(false);

        DistributionAdapterProperties properties = new DistributionAdapterProperties();
        properties.setProvider(DistributionProvider.MONGO);

        ChannelDistributionOutPortDelegate delegate = new ChannelDistributionOutPortDelegate(
                properties,
                List.of(
                        new DistributionAdapterStrategy<>(DistributionProvider.MONGO, mongoAdapter),
                        new DistributionAdapterStrategy<>(DistributionProvider.API, apiAdapter)
                )
        );

        assertTrue(delegate.existsById("channel_01"));

        properties.setProvider(DistributionProvider.API);
        assertFalse(delegate.existsById("channel_01"));
    }

    @Test
    @DisplayName("existsById should fail when configured provider has no strategy")
    void shouldFailWhenProviderStrategyIsMissing() {
        ChannelDistributionOutPort mongoAdapter = mock(ChannelDistributionOutPort.class);

        DistributionAdapterProperties properties = new DistributionAdapterProperties();
        properties.setProvider(DistributionProvider.API);

        ChannelDistributionOutPortDelegate delegate = new ChannelDistributionOutPortDelegate(
                properties,
                List.of(new DistributionAdapterStrategy<>(DistributionProvider.MONGO, mongoAdapter))
        );

        assertThrows(IllegalStateException.class, () -> delegate.existsById("channel_01"));
    }

    @Test
    @DisplayName("constructor should reject null arguments")
    void shouldRejectNullArguments() {
        assertThrows(NullPointerException.class, () -> new ChannelDistributionOutPortDelegate(null, List.of()));
        assertThrows(NullPointerException.class,
                () -> new ChannelDistributionOutPortDelegate(new DistributionAdapterProperties(), null));
    }
}

