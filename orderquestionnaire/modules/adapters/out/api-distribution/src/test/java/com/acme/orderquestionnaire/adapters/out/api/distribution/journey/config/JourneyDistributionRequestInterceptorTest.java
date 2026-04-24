package com.acme.orderquestionnaire.adapters.out.api.distribution.journey.config;

import com.acme.shared.stereotypes.test.UnitTest;
import feign.RequestTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("JourneyDistributionRequestInterceptor")
class JourneyDistributionRequestInterceptorTest {

    @Test
    @DisplayName("apply should always add Accept header")
    void shouldAddAcceptHeader() {
        JourneyDistributionRequestInterceptor interceptor = new JourneyDistributionRequestInterceptor(false);
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertEquals(MediaType.APPLICATION_JSON_VALUE, template.headers().get(HttpHeaders.ACCEPT).iterator().next());
    }

    @Test
    @DisplayName("apply should include authorization header when authentication is enabled")
    void shouldAddAuthorizationWhenAuthenticationEnabled() {
        JourneyDistributionRequestInterceptor interceptor = new JourneyDistributionRequestInterceptor(true);
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertTrue(template.headers().containsKey(HttpHeaders.AUTHORIZATION));
    }

    @Test
    @DisplayName("apply should not include authorization header when authentication is disabled")
    void shouldNotAddAuthorizationWhenAuthenticationDisabled() {
        JourneyDistributionRequestInterceptor interceptor = new JourneyDistributionRequestInterceptor(false);
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertFalse(template.headers().containsKey(HttpHeaders.AUTHORIZATION));
    }
}

