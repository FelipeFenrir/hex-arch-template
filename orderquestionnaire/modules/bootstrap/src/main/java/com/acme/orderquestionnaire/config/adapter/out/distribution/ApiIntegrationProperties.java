package com.acme.orderquestionnaire.config.adapter.out.distribution;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Data
@NoArgsConstructor
public class ApiIntegrationProperties {

    private String baseUrl = "http://localhost:8082";
    private Duration connectTimeout = Duration.ofSeconds(2);
    private Duration readTimeout = Duration.ofSeconds(3);
    private boolean authentication;
}

