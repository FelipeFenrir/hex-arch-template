package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.orderquestionnaire.application.questionnaire.service.context.ValidateQuestionnaireAnswersPipelineContext;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireFactory;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Guard;
import com.acme.shared.pattern.result.Result;

import java.util.List;

/**
 * Validates the validate-answers command fields before any I/O is executed.
 */
public class ValidateQuestionnaireAnswersCommandStep implements Step<ValidateQuestionnaireAnswersPipelineContext> {

    @Override
    public String id() {
        return "VALIDATE_ANSWERS_COMMAND";
    }

    @Override
    public Result<Void, List<DomainError>> execute(ValidateQuestionnaireAnswersPipelineContext context) {
        if (context.command() == null) {
            return QuestionnaireErrors.INVALID_COMMAND.asFailure();
        }

        var command = context.command();
        return Guard.collect(List.of(
                QuestionnaireFactory.validateIdentityPayload(
                        command.questionnaireId(),
                        command.channelDistributionId(),
                        command.journeyDistributionId()
                ),
                Guard.requireNonNull(command.answers(), QuestionnaireErrors.INVALID_ANSWERS)
        ));
    }
}

