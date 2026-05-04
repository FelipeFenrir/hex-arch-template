package com.acme.observability;

import com.acme.observability.config.ObservabilityLoggingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LogSanitizer {

    private static final String MASK = "***";

    private final ObjectMapper mapper;
    private final ObservabilityLoggingProperties properties;

    public LogSanitizer(ObjectMapper mapper, ObservabilityLoggingProperties properties) {
        this.mapper = mapper;
        this.properties = properties;
    }

    public Object sanitize(Object payload) {
        if (payload == null) {
            return null;
        }

        try {
            Object normalized = mapper.convertValue(payload, Object.class);
            return truncate(maskRecursive(normalized));
        } catch (IllegalArgumentException ex) {
            return truncate(String.valueOf(payload));
        }
    }

    private Object maskRecursive(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Map<?, ?> sourceMap) {
            Map<String, Object> maskedMap = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {
                String key = String.valueOf(entry.getKey());
                Object maskedValue = isSensitiveKey(key) ? MASK : maskRecursive(entry.getValue());
                maskedMap.put(key, maskedValue);
            }
            return maskedMap;
        }

        if (value instanceof Collection<?> collection) {
            List<Object> maskedList = new ArrayList<>(collection.size());
            for (Object item : collection) {
                maskedList.add(maskRecursive(item));
            }
            return maskedList;
        }

        if (value.getClass().isArray()) {
            int size = Array.getLength(value);
            List<Object> maskedArray = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                maskedArray.add(maskRecursive(Array.get(value, i)));
            }
            return maskedArray;
        }

        return value;
    }

    private boolean isSensitiveKey(String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            return false;
        }

        String normalizedField = fieldName.trim().toLowerCase(Locale.ROOT);
        for (String configuredField : properties.getSensitiveFields()) {
            if (normalizedField.equals(configuredField.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }

        return false;
    }

    private Object truncate(Object payload) {
        int maxLength = properties.getMaxPayloadLength();
        if (maxLength <= 0) {
            return payload;
        }

        try {
            String json = mapper.writeValueAsString(payload);
            if (json.length() <= maxLength) {
                return payload;
            }
            return json.substring(0, maxLength) + "...(truncated)";
        } catch (Exception ex) {
            String fallback = String.valueOf(payload);
            if (fallback.length() <= maxLength) {
                return fallback;
            }
            return fallback.substring(0, maxLength) + "...(truncated)";
        }
    }
}

