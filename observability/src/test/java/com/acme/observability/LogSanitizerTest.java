package com.acme.observability;

import com.acme.observability.config.ObservabilityLoggingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogSanitizerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldMaskSensitiveFieldsRecursively() {
        ObservabilityLoggingProperties properties = new ObservabilityLoggingProperties();
        properties.setMaxPayloadLength(0);

        LogSanitizer sanitizer = new LogSanitizer(mapper, properties);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("email", "alice@acme.com");
        payload.put("profile", Map.of("password", "top-secret"));
        payload.put("items", List.of(Map.of("authorization", "Bearer 123")));

        Object sanitized = sanitizer.sanitize(payload);
        Map<?, ?> root = assertInstanceOf(Map.class, sanitized);

        assertEquals("***", root.get("email"));
        assertEquals("***", ((Map<?, ?>) root.get("profile")).get("password"));
        Object firstItem = ((List<?>) root.get("items")).get(0);
        assertEquals("***", ((Map<?, ?>) firstItem).get("authorization"));
    }

    @Test
    void shouldMaskKeysCaseInsensitively() {
        ObservabilityLoggingProperties properties = new ObservabilityLoggingProperties();
        properties.setMaxPayloadLength(0);

        LogSanitizer sanitizer = new LogSanitizer(mapper, properties);

        Map<String, Object> payload = Map.of("AccessToken", "abc");

        Object sanitized = sanitizer.sanitize(payload);
        Map<?, ?> root = assertInstanceOf(Map.class, sanitized);

        assertEquals("***", root.get("AccessToken"));
    }

    @Test
    void shouldTruncateWhenPayloadExceedsMaxLength() {
        ObservabilityLoggingProperties properties = new ObservabilityLoggingProperties();
        properties.setMaxPayloadLength(20);

        LogSanitizer sanitizer = new LogSanitizer(mapper, properties);

        Object sanitized = sanitizer.sanitize(Map.of("value", "abcdefghijklmnopqrstuvwxyz"));

        String truncated = assertInstanceOf(String.class, sanitized);
        assertTrue(truncated.endsWith("...(truncated)"));
    }
}

