package com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.AnswerConfigParam;
import com.acme.orderquestionnaire.domain.questionnaire.answer.AnswerOptionItem;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public record AnswerConfigRequest(
        String type,
        Map<String, Object> attributes
) {
    public AnswerConfigParam toParam() {
        String resolvedType = requireString(type, "type").toUpperCase(Locale.ROOT);
        Map<String, Object> attrs = attributes == null ? Map.of() : attributes;

        return switch (resolvedType) {
            case "TEXT" -> new AnswerConfigParam.Text(
                    optionalString(attrs.get("regexPattern")),
                    optionalString(attrs.get("customErrorMessage"))
            );
            case "NUMBER" -> new AnswerConfigParam.Number(
                    toDouble(attrs.get("min")),
                    toDouble(attrs.get("max")),
                    toDouble(attrs.get("step")),
                    toBoolean(attrs.get("allowedDecimal")),
                    toBoolean(attrs.get("allowedNegative")),
                    optionalString(attrs.get("customErrorMessage"))
            );
            case "DATE" -> new AnswerConfigParam.Date(
                    optionalString(attrs.get("maskFormat")),
                    toBoolean(attrs.get("allowPastDates")),
                    optionalString(attrs.get("customErrorMessage"))
            );
            case "OPTION_LIST" -> new AnswerConfigParam.OptionList(
                    toOptions(attrs.get("answerOptions")),
                    optionalString(attrs.get("customErrorMessage"))
            );
            default -> throw new IllegalArgumentException("unsupported answerConfig.type: " + type);
        };
    }

    private List<AnswerOptionItem> toOptions(Object value) {
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof List<?> listValue)) {
            throw new IllegalArgumentException("answerConfiguration.answerOptions must be an array");
        }

        return listValue.stream().map(item -> {
            if (!(item instanceof Map<?, ?> mapItem)) {
                throw new IllegalArgumentException("each answerConfiguration.answerOptions item must be an object");
            }
            String optionValue = requireString(mapItem.get("value"), "answerConfiguration.answerOptions[].value");
            String optionLabel = requireString(mapItem.get("label"), "answerConfiguration.answerOptions[].label");
            return AnswerOptionItem.createNew(optionValue, optionLabel);
        }).toList();
    }

    private Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        throw new IllegalArgumentException("numeric attribute must be a number");
    }

    private boolean toBoolean(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        throw new IllegalArgumentException("boolean attribute must be true/false");
    }

    private String optionalString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String stringValue) {
            return stringValue;
        }
        throw new IllegalArgumentException("string attribute must be text");
    }

    private String requireString(Object value, String fieldName) {
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return stringValue;
        }
        throw new IllegalArgumentException(fieldName + " must be a non-blank string");
    }
}
