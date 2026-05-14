package com.acme.orderquestionnaire.domain.questionnaire.answer.strategy;

import com.acme.orderquestionnaire.domain.questionnaire.answer.AnswerOptionItem;

import java.util.List;

public class AnswerConfigurationFactory {

    // Text Strategy
    public static AnswerTextStrategy createTextStrategy() {
        return AnswerTextStrategy.builder().build();
    }

    public static AnswerTextStrategy createTextStrategy(String regexPattern) {
        return AnswerTextStrategy.builder()
                .withRegexPatternValue(regexPattern)
                .build();
    }

    public static AnswerTextStrategy createTextStrategy(String regexPattern, String customErrorMessage) {
        return AnswerTextStrategy.builder()
                .withRegexPatternValue(regexPattern)
                .withCustomErrorMessage(customErrorMessage)
                .build();
    }

    // Number Strategy
    public static AnswerNumberStrategy createNumberStrategy() {
        return AnswerNumberStrategy.builder()
                .withAllowedDecimal(true)
                .withAllowedNegative(true)
                .build();
    }

    public static AnswerNumberStrategy createNumberStrategy(Double min, Double max) {
        return AnswerNumberStrategy.builder()
                .withMin(min)
                .withMax(max)
                .withAllowedDecimal(true)
                .withAllowedNegative(true)
                .build();
    }

    public static AnswerNumberStrategy createNumberStrategy(
            Double min,
            Double max,
            Double step,
            boolean allowedDecimal,
            boolean allowedNegative,
            String customErrorMessage) {
        return AnswerNumberStrategy.builder()
                .withMin(min)
                .withMax(max)
                .withStep(step)
                .withAllowedDecimal(allowedDecimal)
                .withAllowedNegative(allowedNegative)
                .withCustomErrorMessage(customErrorMessage)
                .build();
    }

    // Date Strategy
    public static AnswerDateStrategy createDateStrategy() {
        return AnswerDateStrategy.builder()
                .withAllowPastDates(true)
                .build();
    }

    public static AnswerDateStrategy createDateStrategy(String maskFormat, boolean allowPastDates) {
        return AnswerDateStrategy.builder()
                .withMaskFormat(maskFormat)
                .withAllowPastDates(allowPastDates)
                .build();
    }

    public static AnswerDateStrategy createDateStrategy(
            String maskFormat,
            boolean allowPastDates,
            String customErrorMessage) {
        return AnswerDateStrategy.builder()
                .withMaskFormat(maskFormat)
                .withAllowPastDates(allowPastDates)
                .withCustomErrorMessage(customErrorMessage)
                .build();
    }

    // List Strategy
    public static AnswerOptionListStrategy createListStrategy(List<AnswerOptionItem> options) {
        return AnswerOptionListStrategy.builder()
                .withAnswerOptions(options)
                .build();
    }

    public static AnswerOptionListStrategy createListStrategy(
            List<AnswerOptionItem> options,
            String customErrorMessage) {
        AnswerOptionListStrategy strategy = AnswerOptionListStrategy.builder()
                .withAnswerOptions(options)
                .build();
        strategy.setCustomErrorMessage(customErrorMessage);
        return strategy;
    }

    public static AnswerOptionListStrategy createListStrategy(AnswerOptionItem... options) {
        return createListStrategy(List.of(options));
    }

    public static AnswerOptionListStrategy createListStrategy(String customErrorMessage, AnswerOptionItem... options) {
        return createListStrategy(List.of(options), customErrorMessage);
    }
}
