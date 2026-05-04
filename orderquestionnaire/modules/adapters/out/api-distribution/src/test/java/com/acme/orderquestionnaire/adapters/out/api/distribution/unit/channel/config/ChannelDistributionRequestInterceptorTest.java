package com.acme.orderquestionnaire.adapters.out.api.distribution.unit.channel.config;

import com.acme.orderquestionnaire.adapters.out.api.distribution.channel.config.ChannelDistributionRequestInterceptor;
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
@DisplayName("ChannelDistributionRequestInterceptor")
class ChannelDistributionRequestInterceptorTest {

    @Test
    @DisplayName("apply should always add Accept header")
    void shouldAddAcceptHeader() {
        ChannelDistributionRequestInterceptor interceptor = new ChannelDistributionRequestInterceptor(false);
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertEquals(MediaType.APPLICATION_JSON_VALUE, template.headers().get(HttpHeaders.ACCEPT).iterator().next());
    }

    @Test
    @DisplayName("apply should include authorization header when authentication is enabled")
    void shouldAddAuthorizationWhenAuthenticationEnabled() {
        ChannelDistributionRequestInterceptor interceptor = new ChannelDistributionRequestInterceptor(true);
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertTrue(template.headers().containsKey(HttpHeaders.AUTHORIZATION));
    }

    @Test
    @DisplayName("apply should not include authorization header when authentication is disabled")
    void shouldNotAddAuthorizationWhenAuthenticationDisabled() {
        ChannelDistributionRequestInterceptor interceptor = new ChannelDistributionRequestInterceptor(false);
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertFalse(template.headers().containsKey(HttpHeaders.AUTHORIZATION));
    }
}

