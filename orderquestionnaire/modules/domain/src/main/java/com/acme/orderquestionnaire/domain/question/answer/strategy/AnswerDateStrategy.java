package com.acme.orderquestionnaire.domain.question.answer.strategy;

import com.acme.orderquestionnaire.domain.question.enumerator.AnswerType;
import com.acme.orderquestionnaire.domain.question.errors.QuestionDomainErrors;
import com.acme.orderquestionnaire.domain.question.answer.AnswerConfiguration;
import com.acme.orderquestionnaire.domain.questionnaire.tree.AnswerConfigurationTreeNode;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
@Builder(setterPrefix = "with", access = AccessLevel.PACKAGE)
public class AnswerDateStrategy implements AnswerConfiguration {
    private String maskFormat;
    private boolean allowPastDates;
    private String customErrorMessage;

    @Override
    public Result<Void, List<DomainError>> validate(Object answer) {
        List<DomainError> errors = new ArrayList<>();

        if (!(answer instanceof LocalDate date)) {
            errors.add(QuestionDomainErrors.invalidAnswerType("date"));
            return Result.failure(errors);
        }

        if (!allowPastDates && date.isBefore(LocalDate.now())) {
            errors.add(QuestionDomainErrors.pastDateNotAllowed());
        }

        return errors.isEmpty() ? Result.success(null) : Result.failure(errors);
    }

    @Override
    public AnswerType getConfigurationType() { return AnswerType.DATE; }

    @Override
    public AnswerConfigurationTreeNode toTreeNode() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("maskFormat", maskFormat);
        attributes.put("allowPastDates", allowPastDates);
        attributes.put("customErrorMessage", customErrorMessage);
        return new AnswerConfigurationTreeNode(getConfigurationType(), attributes);
    }
}

