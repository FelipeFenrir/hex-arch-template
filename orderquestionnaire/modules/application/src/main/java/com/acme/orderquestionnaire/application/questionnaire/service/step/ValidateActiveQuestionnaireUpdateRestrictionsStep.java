package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.orderquestionnaire.application.questionnaire.service.context.UpdateQuestionnairePipelineContext;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;

public class ValidateActiveQuestionnaireUpdateRestrictionsStep implements Step<UpdateQuestionnairePipelineContext> {

    @Override
    public String id() {
        return "VALIDATE_ACTIVE_UPDATE_RESTRICTIONS";
    }

    @Override
    public Result<Void, List<DomainError>> execute(UpdateQuestionnairePipelineContext context) {
        var existing = context.existingQuestionnaire();
        var command = context.command();

        if (existing.status() != ParameterizationStatus.ACTIVE) {
            return Result.success(null);
        }

        boolean hasStructuralChanges = hasStructuralChanges(command);
        boolean hasDescriptionChange = command.description() != null && !command.description().equals(existing.description());
        boolean wantsDeactivate = command.status() == ParameterizationStatus.INACTIVE;

        if (wantsDeactivate && !hasStructuralChanges && !hasDescriptionChange) {
            return Result.success(null);
        }

        return QuestionnaireErrors.QUESTIONNAIRE_UPDATE_NOT_ALLOWED.asFailure();
    }

    private boolean hasStructuralChanges(com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateQuestionnaireCommand command) {
        return (command.questionsToUpsert() != null && !command.questionsToUpsert().isEmpty())
                || (command.questionIdsToRemove() != null && !command.questionIdsToRemove().isEmpty());
    }
}

