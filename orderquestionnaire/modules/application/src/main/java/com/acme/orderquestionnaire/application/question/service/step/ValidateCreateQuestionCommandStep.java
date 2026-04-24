package com.acme.orderquestionnaire.application.question.service.step;

import com.acme.orderquestionnaire.application.question.error.QuestionErrors;
import com.acme.orderquestionnaire.application.question.service.context.CreateQuestionPipelineContext;
import com.acme.orderquestionnaire.domain.question.QuestionFactory;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Guard;
import com.acme.shared.pattern.result.Result;

import java.util.List;

public class ValidateCreateQuestionCommandStep implements Step<CreateQuestionPipelineContext> {

    @Override
    public String id() {
        return "VALIDATE_COMMAND";
    }

    @Override
    public Result<Void, List<DomainError>> execute(CreateQuestionPipelineContext context) {
        if (context.command() == null) {
            return QuestionErrors.INVALID_COMMAND.asFailure();
        }

        var command = context.command();
        return Guard.collect(List.of(
                QuestionFactory.validateCreatePayload(
                        command.id(),
                        command.label(),
                        command.salesItemReferenceCode()
                )
        ));
    }
}

