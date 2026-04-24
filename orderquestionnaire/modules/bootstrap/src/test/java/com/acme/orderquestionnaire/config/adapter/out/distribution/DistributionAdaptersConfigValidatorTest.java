package com.acme.orderquestionnaire.config.adapter.out.distribution;

import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

        assertThrows(IllegalStateException.class, validator::validate);
    }

    @Test
    @DisplayName("validate should reject API provider without base-url")
    void shouldRejectApiWithoutBaseUrl() {
        OrderQuestionnaireOutAdaptersProperties properties = new OrderQuestionnaireOutAdaptersProperties();
        properties.getJourneyDistribution().setProvider(DistributionProvider.API);
        properties.getJourneyDistribution().getIntegrations().getApi().setBaseUrl("   ");

        DistributionAdaptersConfigValidator validator = new DistributionAdaptersConfigValidator(properties);

        assertThrows(IllegalStateException.class, validator::validate);
    }

    @Test
    @DisplayName("constructor should reject null properties")
    void shouldRejectNullProperties() {
        assertThrows(NullPointerException.class, () -> new DistributionAdaptersConfigValidator(null));
    }
}

