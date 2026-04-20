package com.acme.orderquestionnaire.application.questionnaire.service.support.condition;

import com.acme.orderquestionnaire.application.question.error.QuestionErrors;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.ConditionParam;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.CompositeCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.QuestionCondition;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;
import java.util.function.Function;

final class CompositeConditionAssemblerStrategy implements ConditionAssemblerStrategy {

    @Override
    public boolean supports(ConditionParam param) {
        return param instanceof ConditionParam.Composite;
    }

    @Override
    public Result<QuestionCondition, List<DomainError>> assemble(ConditionParam param,
                                                                 Function<ConditionParam, Result<QuestionCondition, List<DomainError>>> resolver) {
        ConditionParam.Composite compositeParam = (ConditionParam.Composite) param;
        if (compositeParam.conditions() == null || compositeParam.conditions().isEmpty()) {
            return QuestionErrors.INVALID_COMMAND.asFailure();
        }

        CompositeCondition compositeCondition = new CompositeCondition(compositeParam.andOperator());
        for (ConditionParam childParam : compositeParam.conditions()) {
            Result<QuestionCondition, List<DomainError>> childResult = resolver.apply(childParam);
            if (childResult.isFailure()) {
                return Result.failure(childResult.errorOrElseThrow(() ->
                        new IllegalStateException("Expected failure result")));
            }
            compositeCondition.addCondition(childResult.getOrElseThrow(error ->
                    new IllegalStateException("Expected success result")));
        }
        return Result.success(compositeCondition);
    }
}
