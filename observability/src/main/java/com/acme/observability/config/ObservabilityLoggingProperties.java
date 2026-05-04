package com.acme.observability.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashSet;
import java.util.Set;

@ConfigurationProperties(prefix = "acme.observability.logging")
public class ObservabilityLoggingProperties {

    private boolean enabled = true;
    private boolean logArguments = true;
    private boolean logResult = true;
    private int maxPayloadLength = 4000;
    private Set<String> sensitiveFields = new LinkedHashSet<>(Set.of(
            "password",
            "passwd",
            "secret",
            "token",
            "accessToken",
            "refreshToken",
            "authorization",
            "apiKey",
            "clientSecret",
            "cpf",
            "document",
            "email",
            "phone"
    ));

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isLogArguments() {
        return logArguments;
    }

    public void setLogArguments(boolean logArguments) {
        this.logArguments = logArguments;
    }

    public boolean isLogResult() {
        return logResult;
    }

    public void setLogResult(boolean logResult) {
        this.logResult = logResult;
    }

    public int getMaxPayloadLength() {
        return maxPayloadLength;
    }

    public void setMaxPayloadLength(int maxPayloadLength) {
        this.maxPayloadLength = maxPayloadLength;
    }

    public Set<String> getSensitiveFields() {
        return sensitiveFields;
    }

    public void setSensitiveFields(Set<String> sensitiveFields) {
        this.sensitiveFields = sensitiveFields;
    }
}

