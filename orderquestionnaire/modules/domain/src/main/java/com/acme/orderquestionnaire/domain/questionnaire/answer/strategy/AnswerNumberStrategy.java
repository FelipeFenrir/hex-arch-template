package com.acme.orderquestionnaire.domain.questionnaire.answer.strategy;

import com.acme.orderquestionnaire.domain.question.enumerator.AnswerType;
import com.acme.orderquestionnaire.domain.question.errors.QuestionDomainErrors;
import com.acme.orderquestionnaire.domain.questionnaire.answer.AnswerConfiguration;
import com.acme.orderquestionnaire.domain.questionnaire.tree.AnswerConfigurationTreeNode;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
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
public class AnswerNumberStrategy implements AnswerConfiguration {
    private Double min;
    private Double max;
    private Double step;
    private boolean allowedDecimal;
    private boolean allowedNegative;
    private String customErrorMessage;

    @Override
    public Result<Void, List<DomainError>> validate(Object answer) {
        List<DomainError> errors = new ArrayList<>();

        if (!(answer instanceof Number)) {
            errors.add(QuestionDomainErrors.invalidAnswerType("number"));
            return Result.failure(errors);
        }

        double value = ((Number) answer).doubleValue();
        if (!allowedDecimal && value % 1 != 0)     errors.add(QuestionDomainErrors.decimalNotAllowed());
        if (!allowedNegative && value < 0)          errors.add(QuestionDomainErrors.negativeNotAllowed());
        if (min != null && value < min)             errors.add(QuestionDomainErrors.valueBelowMin());
        if (max != null && value > max)             errors.add(QuestionDomainErrors.valueAboveMax());
        if (step != null && value % step != 0)      errors.add(QuestionDomainErrors.invalidStep(step));

        return errors.isEmpty() ? Result.success(null) : Result.failure(errors);
    }

    @Override
    public AnswerType getConfigurationType() { return AnswerType.NUMBER; }

    @Override
    public AnswerConfigurationTreeNode toTreeNode() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("min", min);
        attributes.put("max", max);
        attributes.put("step", step);
        attributes.put("allowedDecimal", allowedDecimal);
        attributes.put("allowedNegative", allowedNegative);
        attributes.put("customErrorMessage", customErrorMessage);
        return new AnswerConfigurationTreeNode(getConfigurationType(), attributes);
    }
}

