package com.acme.orderquestionnaire.unit;

import com.acme.orderquestionnaire.application.channel.port.out.ChannelDistributionOutPort;
import com.acme.orderquestionnaire.application.journey.port.out.JourneyDistributionOutPort;
import com.acme.orderquestionnaire.config.adapter.out.distribution.DistributionAdapterStrategy;
import com.acme.orderquestionnaire.config.adapter.out.distribution.DistributionAdaptersConfigValidator;
import com.acme.orderquestionnaire.config.adapter.out.distribution.DistributionOutPortConfiguration;
import com.acme.orderquestionnaire.config.adapter.out.distribution.DistributionProvider;
import com.acme.orderquestionnaire.config.adapter.out.distribution.OrderQuestionnaireOutAdaptersProperties;
import com.acme.orderquestionnaire.config.adapter.out.distribution.channel.ChannelDistributionOutPortDelegate;
import com.acme.orderquestionnaire.config.adapter.out.distribution.journey.JourneyDistributionOutPortDelegate;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@DisplayName("DistributionOutPortConfiguration")
class DistributionOutPortConfigurationTest {

    private final DistributionOutPortConfiguration configuration = new DistributionOutPortConfiguration();

    @Test
    @DisplayName("channel strategy beans should bind provider and adapter")
    void shouldCreateChannelStrategies() {
        ChannelDistributionOutPort mongoAdapter = mock(ChannelDistributionOutPort.class);
        ChannelDistributionOutPort apiAdapter = mock(ChannelDistributionOutPort.class);

        DistributionAdapterStrategy<ChannelDistributionOutPort> mongoStrategy =
                configuration.channelDistributionMongoStrategy(mongoAdapter);
        DistributionAdapterStrategy<ChannelDistributionOutPort> apiStrategy =
                configuration.channelDistributionApiStrategy(apiAdapter);

        assertEquals(DistributionProvider.MONGO, mongoStrategy.provider());
        assertEquals(DistributionProvider.API, apiStrategy.provider());
        assertSame(mongoAdapter, mongoStrategy.adapter());
        assertSame(apiAdapter, apiStrategy.adapter());
    }

    @Test
    @DisplayName("journey strategy beans should bind provider and adapter")
    void shouldCreateJourneyStrategies() {
        JourneyDistributionOutPort mongoAdapter = mock(JourneyDistributionOutPort.class);
        JourneyDistributionOutPort apiAdapter = mock(JourneyDistributionOutPort.class);

        DistributionAdapterStrategy<JourneyDistributionOutPort> mongoStrategy =
                configuration.journeyDistributionMongoStrategy(mongoAdapter);
        DistributionAdapterStrategy<JourneyDistributionOutPort> apiStrategy =
                configuration.journeyDistributionApiStrategy(apiAdapter);

        assertEquals(DistributionProvider.MONGO, mongoStrategy.provider());
        assertEquals(DistributionProvider.API, apiStrategy.provider());
        assertSame(mongoAdapter, mongoStrategy.adapter());
        assertSame(apiAdapter, apiStrategy.adapter());
    }

    @Test
    @DisplayName("channel out port bean should return delegate wired with selected provider")
    void shouldCreateChannelOutPortDelegate() {
        ChannelDistributionOutPort mongoAdapter = mock(ChannelDistributionOutPort.class);
        ChannelDistributionOutPort apiAdapter = mock(ChannelDistributionOutPort.class);
        when(apiAdapter.existsById("channel_01")).thenReturn(true);

        OrderQuestionnaireOutAdaptersProperties properties = new OrderQuestionnaireOutAdaptersProperties();
        properties.getChannelDistribution().setProvider(DistributionProvider.API);

        ChannelDistributionOutPort outPort = configuration.channelDistributionOutPort(
                properties,
                List.of(
                        configuration.channelDistributionMongoStrategy(mongoAdapter),
                        configuration.channelDistributionApiStrategy(apiAdapter)
                )
        );

        assertInstanceOf(ChannelDistributionOutPortDelegate.class, outPort);
        assertTrue(outPort.existsById("channel_01"));
    }

    @Test
    @DisplayName("journey out port bean should return delegate wired with selected provider")
    void shouldCreateJourneyOutPortDelegate() {
        JourneyDistributionOutPort mongoAdapter = mock(JourneyDistributionOutPort.class);
        JourneyDistributionOutPort apiAdapter = mock(JourneyDistributionOutPort.class);
        when(apiAdapter.existsById("journey_01")).thenReturn(true);

        OrderQuestionnaireOutAdaptersProperties properties = new OrderQuestionnaireOutAdaptersProperties();
        properties.getJourneyDistribution().setProvider(DistributionProvider.API);

        JourneyDistributionOutPort outPort = configuration.journeyDistributionOutPort(
                properties,
                List.of(
                        configuration.journeyDistributionMongoStrategy(mongoAdapter),
                        configuration.journeyDistributionApiStrategy(apiAdapter)
                )
        );

        assertInstanceOf(JourneyDistributionOutPortDelegate.class, outPort);
        assertTrue(outPort.existsById("journey_01"));
    }

    @Test
    @DisplayName("validator bean should be created and validate default properties")
    void shouldCreateValidatorBean() {
        OrderQuestionnaireOutAdaptersProperties properties = new OrderQuestionnaireOutAdaptersProperties();

        DistributionAdaptersConfigValidator validator = configuration.distributionAdaptersConfigValidator(properties);

        assertInstanceOf(DistributionAdaptersConfigValidator.class, validator);
        assertDoesNotThrow(validator::validate);
    }
}

