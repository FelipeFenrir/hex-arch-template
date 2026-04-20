package com.acme.orderquestionnaire.application.questionnaire.service.support.condition;

import com.acme.orderquestionnaire.application.question.error.QuestionErrors;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.ConditionParam;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.EqualCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.QuestionCondition;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;
import java.util.function.Function;

final class EqualConditionAssemblerStrategy implements ConditionAssemblerStrategy {

    @Override
    public boolean supports(ConditionParam param) {
        return param instanceof ConditionParam.Equal;
    }

    @Override
    public Result<QuestionCondition, List<DomainError>> assemble(ConditionParam param,
                                                                 Function<ConditionParam, Result<QuestionCondition, List<DomainError>>> resolver) {
        ConditionParam.Equal equal = (ConditionParam.Equal) param;
        if (equal.questionRootCode() == null || equal.questionRootCode().isBlank()) {
            return QuestionErrors.INVALID_COMMAND.asFailure();
        }
        return Result.success(new EqualCondition(equal.questionRootCode(), equal.expectedValue()));
    }
}


