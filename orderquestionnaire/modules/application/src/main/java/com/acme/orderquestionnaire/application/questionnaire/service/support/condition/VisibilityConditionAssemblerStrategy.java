package com.acme.orderquestionnaire.application.questionnaire.service.support.condition;

import com.acme.orderquestionnaire.application.question.error.QuestionErrors;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.ConditionParam;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.QuestionCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.VisibilityCondition;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;
import java.util.function.Function;

final class VisibilityConditionAssemblerStrategy implements ConditionAssemblerStrategy {

    @Override
    public boolean supports(ConditionParam param) {
        return param instanceof ConditionParam.Visibility;
    }

    @Override
    public Result<QuestionCondition, List<DomainError>> assemble(ConditionParam param,
                                                                 Function<ConditionParam, Result<QuestionCondition, List<DomainError>>> resolver) {
        ConditionParam.Visibility visibility = (ConditionParam.Visibility) param;
        if (visibility.questionRootCode() == null || visibility.questionRootCode().isBlank()) {
            return QuestionErrors.INVALID_COMMAND.asFailure();
        }
        return Result.success(new VisibilityCondition(visibility.questionRootCode(), visibility.expectedValue()));
    }
}


