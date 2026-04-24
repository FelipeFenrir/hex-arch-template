package com.acme.orderquestionnaire.adapters.out.api.distribution.channel.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class ChannelDistributionRequestInterceptor implements RequestInterceptor {

    private final boolean authenticationEnabled;

    public ChannelDistributionRequestInterceptor(boolean authenticationEnabled) {
        this.authenticationEnabled = authenticationEnabled;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        if (authenticationEnabled) {
            template.header(HttpHeaders.AUTHORIZATION, "Bearer pending-token");
        }
    }
}



