package com.acme.orderquestionnaire.unit;

import com.acme.orderquestionnaire.config.adapter.out.distribution.ApiIntegrationProperties;
import com.acme.orderquestionnaire.config.adapter.out.distribution.DistributionAdapterProperties;
import com.acme.orderquestionnaire.config.adapter.out.distribution.DistributionIntegrationsProperties;
import com.acme.orderquestionnaire.config.adapter.out.distribution.DistributionProvider;
import com.acme.orderquestionnaire.config.adapter.out.distribution.OrderQuestionnaireOutAdaptersProperties;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("Distribution properties model")
class DistributionPropertiesModelTest {

    @Test
    @DisplayName("provider enum should expose enabled status")
    void shouldExposeProviderEnabledStatus() {
        assertTrue(DistributionProvider.MONGO.isEnabled());
        assertTrue(DistributionProvider.API.isEnabled());
        assertFalse(DistributionProvider.KAFKA.isEnabled());
    }

    @Test
    @DisplayName("adapter properties should allow custom configuration")
    void shouldConfigureAdapterProperties() {
        DistributionAdapterProperties properties = new DistributionAdapterProperties();
        properties.setProvider(DistributionProvider.API);
        properties.setUseAppDatabase(false);

        ApiIntegrationProperties api = new ApiIntegrationProperties();
        api.setBaseUrl("http://localhost:8082");
        api.setConnectTimeout(Duration.ofSeconds(5));
        api.setReadTimeout(Duration.ofSeconds(10));
        api.setAuthentication(true);

        DistributionIntegrationsProperties integrations = new DistributionIntegrationsProperties();
        integrations.setApi(api);
        properties.setIntegrations(integrations);

        assertEquals(DistributionProvider.API, properties.getProvider());
        assertFalse(properties.isUseAppDatabase());
        assertEquals("http://localhost:8082", properties.getIntegrations().getApi().getBaseUrl());
        assertEquals(Duration.ofSeconds(5), properties.getIntegrations().getApi().getConnectTimeout());
        assertEquals(Duration.ofSeconds(10), properties.getIntegrations().getApi().getReadTimeout());
        assertTrue(properties.getIntegrations().getApi().isAuthentication());
    }

    @Test
    @DisplayName("root properties should initialize both distribution adapter configs")
    void shouldInitializeRootProperties() {
        OrderQuestionnaireOutAdaptersProperties properties = new OrderQuestionnaireOutAdaptersProperties();

        assertNotNull(properties.getChannelDistribution());
        assertNotNull(properties.getJourneyDistribution());
    }
}

