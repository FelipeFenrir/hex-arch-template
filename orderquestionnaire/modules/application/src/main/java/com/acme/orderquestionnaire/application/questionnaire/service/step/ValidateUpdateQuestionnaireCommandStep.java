package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateConfiguredQuestionParam;
import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.orderquestionnaire.application.questionnaire.service.context.UpdateQuestionnairePipelineContext;
import com.acme.orderquestionnaire.domain.question.QuestionFactory;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireFactory;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Guard;
import com.acme.shared.pattern.result.Result;

import java.util.ArrayList;
import java.util.List;

public class ValidateUpdateQuestionnaireCommandStep implements Step<UpdateQuestionnairePipelineContext> {

    @Override
    public String id() {
        return "VALIDATE_COMMAND";
    }

    @Override
    public Result<Void, List<DomainError>> execute(UpdateQuestionnairePipelineContext context) {
        if (context.command() == null) {
            return QuestionnaireErrors.INVALID_COMMAND.asFailure();
        }

        var command = context.command();
        List<Result<Void, List<DomainError>>> validations = new ArrayList<>();
        validations.add(QuestionnaireFactory.validateUpdatePayload(
                command.id(),
                command.channelDistributionId(),
                command.journeyDistributionId(),
                command.description()
        ));

        for (UpdateConfiguredQuestionParam upsert : safeList(command.questionsToUpsert())) {
            validations.add(Guard.requireNonNull(upsert, QuestionnaireErrors.INVALID_COMMAND));
            if (upsert != null) {
                validations.add(QuestionFactory.validateQuestionId(upsert.questionId()));
                validations.add(Guard.requireNonNull(upsert.param(), QuestionnaireErrors.INVALID_COMMAND));
            }
        }

        return Guard.collect(validations);
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }
}

