package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.orderquestionnaire.application.questionnaire.service.context.DeleteQuestionnairePipelineContext;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Guard;
import com.acme.shared.pattern.result.Result;

import java.util.List;

/**
 * Validates the {@link com.acme.orderquestionnaire.application.questionnaire.dto.command.DeleteQuestionnaireCommand}
 * composite identity fields before any I/O is performed.
 *
 * <p>Accumulates ALL field-level errors in a single pass using {@link Guard#collect}.
 * No side-effects; rollback is a no-op (default {@code NONE}).
 */
public class ValidateDeleteQuestionnaireCommandStep implements Step<DeleteQuestionnairePipelineContext> {

    @Override
    public String id() {
        return "VALIDATE_DELETE_COMMAND";
    }

    @Override
    public Result<Void, List<DomainError>> execute(DeleteQuestionnairePipelineContext context) {
        if (context.command() == null) {
            return QuestionnaireErrors.INVALID_COMMAND.asFailure();
        }

        var command = context.command();
        return Guard.collect(List.of(
                Guard.requireNonBlank(command.id(), QuestionnaireErrors.INVALID_ID),
                Guard.requireNonBlank(command.channelDistributionId(), QuestionnaireErrors.INVALID_CHANNEL_DISTRIBUTION_ID),
                Guard.requireNonBlank(command.journeyDistributionId(), QuestionnaireErrors.INVALID_JOURNEY_DISTRIBUTION_ID)
        ));
    }
}

