package com.acme.orderquestionnaire.domain.question.answer.strategy;

import com.acme.orderquestionnaire.domain.question.enumerator.AnswerType;
import com.acme.orderquestionnaire.domain.question.errors.QuestionDomainErrors;
import com.acme.orderquestionnaire.domain.question.answer.AnswerConfiguration;
import com.acme.orderquestionnaire.domain.question.answer.AnswerOptionItem;
import com.acme.orderquestionnaire.domain.questionnaire.tree.AnswerConfigurationTreeNode;
import com.acme.orderquestionnaire.domain.questionnaire.tree.AnswerOptionTreeNode;
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
public class AnswerOptionListStrategy implements AnswerConfiguration {
    private List<AnswerOptionItem> answerOptions;
    private String customErrorMessage;

    @Builder(setterPrefix = "with", access = AccessLevel.PACKAGE)
    public AnswerOptionListStrategy(List<AnswerOptionItem> answerOptions) {
        this.answerOptions = answerOptions;
    }

    @Override
    public Result<Void, List<DomainError>> validate(Object answer) {
        List<DomainError> errors = new ArrayList<>();

        if (!(answer instanceof String answerValue)) {
            errors.add(QuestionDomainErrors.invalidAnswerType("option value (string)"));
            return Result.failure(errors);
        }

        boolean valid = answerOptions.stream()
                .anyMatch(o -> o.value().equals(answerValue));

        if (!valid) errors.add(QuestionDomainErrors.invalidOption());

        return errors.isEmpty() ? Result.success(null) : Result.failure(errors);
    }

    @Override
    public AnswerType getConfigurationType() { return AnswerType.OPTION_LIST; }

    @Override
    public AnswerConfigurationTreeNode toTreeNode() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        List<AnswerOptionTreeNode> optionNodes = answerOptions == null
                ? List.of()
                : answerOptions.stream()
                .map(option -> new AnswerOptionTreeNode(option.value(), option.label()))
                .toList();
        attributes.put("answerOptions", optionNodes);
        attributes.put("customErrorMessage", customErrorMessage);
        return new AnswerConfigurationTreeNode(getConfigurationType(), attributes);
    }
}

