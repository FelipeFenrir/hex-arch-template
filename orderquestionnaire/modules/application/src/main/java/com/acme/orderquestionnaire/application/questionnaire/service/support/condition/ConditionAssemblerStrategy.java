package com.acme.orderquestionnaire.application.questionnaire.service.support.condition;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.ConditionParam;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.QuestionCondition;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;
import java.util.function.Function;

interface ConditionAssemblerStrategy {

    boolean supports(ConditionParam param);

    Result<QuestionCondition, List<DomainError>> assemble(
            ConditionParam param,
            Function<ConditionParam, Result<QuestionCondition, List<DomainError>>> resolver);
}


