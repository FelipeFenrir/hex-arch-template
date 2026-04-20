package com.acme.orderquestionnaire.application.questionnaire.service.support.condition;

import com.acme.orderquestionnaire.application.question.error.QuestionErrors;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.ConditionParam;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.NumericCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.QuestionCondition;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;
import java.util.function.Function;

final class NumericConditionAssemblerStrategy implements ConditionAssemblerStrategy {

    @Override
    public boolean supports(ConditionParam param) {
        return param instanceof ConditionParam.Numeric;
    }

    @Override
    public Result<QuestionCondition, List<DomainError>> assemble(ConditionParam param,
                                                                 Function<ConditionParam, Result<QuestionCondition, List<DomainError>>> resolver) {
        ConditionParam.Numeric numeric = (ConditionParam.Numeric) param;
        if (numeric.questionRootCode() == null || numeric.questionRootCode().isBlank()
                || numeric.expectedValue() == null
                || numeric.operator() == null || numeric.operator().isBlank()) {
            return QuestionErrors.INVALID_COMMAND.asFailure();
        }
        try {
            return Result.success(new NumericCondition(
                    numeric.questionRootCode(),
                    numeric.expectedValue(),
                    numeric.operator()));
        } catch (IllegalArgumentException exception) {
            return QuestionErrors.INVALID_COMMAND.asFailure();
        }
    }
}


