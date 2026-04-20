package com.acme.orderquestionnaire.application.questionnaire.service.support.condition;

import com.acme.orderquestionnaire.application.question.error.QuestionErrors;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.ConditionParam;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.QuestionCondition;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;

public final class ConditionAssembler {

    private static final List<ConditionAssemblerStrategy> STRATEGIES = List.of(
            new EqualConditionAssemblerStrategy(),
            new NumericConditionAssemblerStrategy(),
            new VisibilityConditionAssemblerStrategy(),
            new CompositeConditionAssemblerStrategy()
    );

    private ConditionAssembler() {
        throw new IllegalStateException("Utility class");
    }

    public static Result<QuestionCondition, List<DomainError>> toDomain(ConditionParam param) {
        if (param == null) {
            return Result.success(null);
        }
        return STRATEGIES.stream()
                .filter(strategy -> strategy.supports(param))
                .findFirst()
                .<Result<QuestionCondition, List<DomainError>>>map(strategy -> strategy.assemble(param, ConditionAssembler::toDomain))
                .orElseGet(QuestionErrors.INVALID_COMMAND::asFailure);
    }
}

