package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.orderquestionnaire.application.questionnaire.service.context.CreateQuestionnairePipelineContext;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireFactory;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Guard;
import com.acme.shared.pattern.result.Result;

import java.util.List;

/**
 * Validates the raw {@link com.acme.orderquestionnaire.application.questionnaire.dto.command.CreateQuestionnaireCommand}.
 *
 * <p>Phase 1 — accumulates ALL field-level validation errors before proceeding to I/O checks.
 * Uses {@link Guard#collect} so that the caller receives the full list of violations in one response.
 * No side-effects; rollback is a no-op (default {@code NONE}).
 */
public class ValidateCommandStep implements Step<CreateQuestionnairePipelineContext> {

    @Override
    public String id() {
        return "VALIDATE_COMMAND";
    }

    @Override
    public Result<Void, List<DomainError>> execute(CreateQuestionnairePipelineContext context) {
        if (context.command() == null) {
            return QuestionnaireErrors.INVALID_COMMAND.asFailure();
        }

        var command = context.command();
        return Guard.collect(List.of(
                QuestionnaireFactory.validateCreatePayload(
                        command.id(),
                        command.channelDistributionId(),
                        command.journeyDistributionId(),
                        command.description()
                )
        ));
    }
}

