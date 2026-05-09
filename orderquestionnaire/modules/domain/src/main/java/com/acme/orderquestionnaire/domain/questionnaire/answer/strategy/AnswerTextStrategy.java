package com.acme.orderquestionnaire.domain.questionnaire.answer.strategy;

import com.acme.orderquestionnaire.domain.question.enumerator.AnswerType;
import com.acme.orderquestionnaire.domain.question.errors.QuestionDomainErrors;
import com.acme.orderquestionnaire.domain.questionnaire.answer.AnswerConfiguration;
import com.acme.orderquestionnaire.domain.questionnaire.tree.AnswerConfigurationTreeNode;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.vo.RegexPattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
@Builder(setterPrefix = "with", access = AccessLevel.PACKAGE)
public class AnswerTextStrategy implements AnswerConfiguration {
    private String regexPatternValue;    // Stored as String for builder compatibility
    private String customErrorMessage;

    // Compatibility accessors for existing callers/tests expecting get/setRegexPattern.
    public String getRegexPattern() {
        return regexPatternValue;
    }

    public void setRegexPattern(String regexPattern) {
        this.regexPatternValue = regexPattern;
    }

    /**
     * Backward compatibility accessor: returns the regex pattern as a String value.
     */
    public String regexPattern() {
        return regexPatternValue;
    }

    /**
     * Returns the regex pattern as a Value Object.
     * Use this when working within the domain.
     */
    public RegexPattern asRegexPattern() {
        if (regexPatternValue == null || regexPatternValue.isBlank()) {
            return null;
        }
        return RegexPattern.of(regexPatternValue);
    }

    @Override
    public Result<Void, List<DomainError>> validate(Object answer) {
        List<DomainError> errors = new ArrayList<>();

        if (!(answer instanceof String text)) {
            errors.add(QuestionDomainErrors.invalidAnswerType("string"));
            return Result.failure(errors);
        }

        if (regexPatternValue != null && !text.matches(regexPatternValue)) {
            String message = customErrorMessage != null ? customErrorMessage : "Answer does not match the required format.";
            errors.add(QuestionDomainErrors.patternMismatch(message));
        }

        return errors.isEmpty() ? Result.success(null) : Result.failure(errors);
    }

    @Override
    public AnswerType getConfigurationType() { return AnswerType.TEXT; }

    @Override
    public AnswerConfigurationTreeNode toTreeNode() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("regexPattern", regexPatternValue);
        attributes.put("customErrorMessage", customErrorMessage);
        return new AnswerConfigurationTreeNode(getConfigurationType(), attributes);
    }
}

