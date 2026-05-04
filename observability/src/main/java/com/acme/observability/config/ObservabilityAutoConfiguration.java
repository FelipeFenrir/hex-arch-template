package com.acme.observability.config;

import com.acme.observability.LogSanitizer;
import com.acme.observability.LoggingAspect;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(LoggingAspect.class)
@EnableConfigurationProperties(ObservabilityLoggingProperties.class)
public class ObservabilityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    LogSanitizer logSanitizer(ObjectMapper mapper, ObservabilityLoggingProperties properties) {
        return new LogSanitizer(mapper, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "acme.observability.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
    LoggingAspect loggingAspect(
            ObjectMapper mapper,
            LogSanitizer sanitizer,
            ObservabilityLoggingProperties properties
    ) {
        return new LoggingAspect(mapper, sanitizer, properties);
    }
}

