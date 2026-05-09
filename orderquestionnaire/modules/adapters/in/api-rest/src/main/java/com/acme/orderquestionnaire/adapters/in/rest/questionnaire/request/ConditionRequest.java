package com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.ConditionParam;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public record ConditionRequest(
        String type,
        Map<String, Object> attributes,
        List<ConditionRequest> children
) {
    public ConditionParam toParam() {
        String resolvedType = requireString(type, "type").toUpperCase(Locale.ROOT);
        Map<String, Object> attrs = attributes == null ? Map.of() : attributes;

        return switch (resolvedType) {
            case "EQUAL" -> new ConditionParam.Equal(
                    requireString(attrs.get("questionRootCode"), "condition.attributes.questionRootCode"),
                    attrs.get("expectedValue")
            );
            case "NUMERIC" -> new ConditionParam.Numeric(
                    requireString(attrs.get("questionRootCode"), "condition.attributes.questionRootCode"),
                    requireDouble(attrs.get("expectedValue"), "condition.attributes.expectedValue"),
                    requireString(attrs.get("operator"), "condition.attributes.operator")
            );
            case "VISIBILITY" -> new ConditionParam.Visibility(
                    requireString(attrs.get("questionRootCode"), "condition.attributes.questionRootCode"),
                    attrs.get("expectedValue")
            );
            case "COMPOSITE" -> new ConditionParam.Composite(
                    toAndOperator(attrs.get("operator")),
                    safeChildren(children).stream().map(ConditionRequest::toParam).toList()
            );
            default -> throw new IllegalArgumentException("unsupported condition.type: " + type);
        };
    }

    private boolean toAndOperator(Object value) {
        if (value == null) {
            return true;
        }

        if (value instanceof String stringValue) {
            String normalized = stringValue.trim().toUpperCase(Locale.ROOT);
            return switch (normalized) {
                case "AND" -> true;
                case "OR" -> false;
                default -> throw new IllegalArgumentException("condition.attributes.operator must be AND or OR");
            };
        }

        throw new IllegalArgumentException("condition.attributes.operator must be a string");
    }

    private List<ConditionRequest> safeChildren(List<ConditionRequest> values) {
        return values == null ? List.of() : values;
    }

    private String requireString(Object value, String fieldName) {
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return stringValue;
        }
        throw new IllegalArgumentException(fieldName + " must be a non-blank string");
    }

    private Double requireDouble(Object value, String fieldName) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        throw new IllegalArgumentException(fieldName + " must be a number");
    }
}

