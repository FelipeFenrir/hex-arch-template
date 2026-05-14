package com.acme.orderquestionnaire.unit;

import com.acme.orderquestionnaire.config.adapter.out.distribution.DistributionAdaptersConfigValidator;
import com.acme.orderquestionnaire.config.adapter.out.distribution.DistributionProvider;
import com.acme.orderquestionnaire.config.adapter.out.distribution.OrderQuestionnaireOutAdaptersProperties;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
@DisplayName("DistributionAdaptersConfigValidator")
class DistributionAdaptersConfigValidatorTest {

    @Test
    @DisplayName("validate should pass for default mongo configuration")
    void shouldAcceptDefaultMongoConfiguration() {
        OrderQuestionnaireOutAdaptersProperties properties = new OrderQuestionnaireOutAdaptersProperties();

        DistributionAdaptersConfigValidator validator = new DistributionAdaptersConfigValidator(properties);

        assertDoesNotThrow(validator::validate);
    }

    @Test
    @DisplayName("validate should reject disabled providers")
    void shouldRejectDisabledProviders() {
        OrderQuestionnaireOutAdaptersProperties properties = new OrderQuestionnaireOutAdaptersProperties();
        properties.getChannelDistribution().setProvider(DistributionProvider.GRPC);

        DistributionAdaptersConfigValidator validator = new DistributionAdaptersConfigValidator(properties);

        IllegalStateException exception = assertThrows(IllegalStateException.class, validator::validate);
        assertTrue(exception.getMessage().contains("Provider [GRPC] is not enabled yet for [channel-distribution]"));
    }

    @Test
    @DisplayName("validate should reject API provider without base-url")
    void shouldRejectApiWithoutBaseUrl() {
        OrderQuestionnaireOutAdaptersProperties properties = new OrderQuestionnaireOutAdaptersProperties();
        properties.getJourneyDistribution().setProvider(DistributionProvider.API);
        properties.getJourneyDistribution().getIntegrations().getApi().setBaseUrl("   ");

        DistributionAdaptersConfigValidator validator = new DistributionAdaptersConfigValidator(properties);

        IllegalStateException exception = assertThrows(IllegalStateException.class, validator::validate);
        assertTrue(exception.getMessage().contains("API base-url must be configured for [journey-distribution]"));
    }

    @Test
    @DisplayName("validate should reject null adapter properties")
    void shouldRejectNullAdapterProperties() {
        OrderQuestionnaireOutAdaptersProperties properties = new OrderQuestionnaireOutAdaptersProperties();
        properties.setChannelDistribution(null);

        DistributionAdaptersConfigValidator validator = new DistributionAdaptersConfigValidator(properties);

        NullPointerException exception = assertThrows(NullPointerException.class, validator::validate);
        assertTrue(exception.getMessage().contains("channel-distribution properties must not be null"));
    }

    @Test
    @DisplayName("validate should reject API provider without integrations")
    void shouldRejectApiWithoutIntegrations() {
        OrderQuestionnaireOutAdaptersProperties properties = new OrderQuestionnaireOutAdaptersProperties();
        properties.getJourneyDistribution().setProvider(DistributionProvider.API);
        properties.getJourneyDistribution().setIntegrations(null);

        DistributionAdaptersConfigValidator validator = new DistributionAdaptersConfigValidator(properties);

        NullPointerException exception = assertThrows(NullPointerException.class, validator::validate);
        assertTrue(exception.getMessage().contains("journey-distribution integrations must not be null"));
    }

    @Test
    @DisplayName("constructor should reject null properties")
    void shouldRejectNullProperties() {
        assertThrows(NullPointerException.class, () -> new DistributionAdaptersConfigValidator(null));
    }
}

