package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.orderquestionnaire.application.questionnaire.service.context.DeleteQuestionnairePipelineContext;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;

/**
 * Validates that the fetched {@link Questionnaire} is in a state that permits deletion.
 *
 * <p>Delegates the eligibility check to {@link Questionnaire#canBeDeleted()}, which allows
 * deletion only for {@code DRAFT} and {@code INACTIVE} statuses.
 * No side-effects; rollback is a no-op (default {@code NONE}).
 */
public class ValidateQuestionnaireDeleteEligibilityStep implements Step<DeleteQuestionnairePipelineContext> {

    @Override
    public String id() {
        return "VALIDATE_DELETE_ELIGIBILITY";
    }

    @Override
    public Result<Void, List<DomainError>> execute(DeleteQuestionnairePipelineContext context) {
        Questionnaire questionnaire = context.questionnaire();
        if (!questionnaire.canBeDeleted()) {
            return QuestionnaireErrors.QUESTIONNAIRE_DELETE_NOT_ALLOWED.asFailure(
                    context.command().id(),
                    questionnaire.status().name()
            );
        }
        return Result.success(null);
    }
}

