package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.orderquestionnaire.application.questionnaire.service.context.UpdateQuestionnairePipelineContext;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireStatusMachine;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireStatusTransitionContext;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.vo.AuditInfo;

import java.util.List;

public class ApplyQuestionnaireUpdateTransitionStep implements Step<UpdateQuestionnairePipelineContext> {

    @Override
    public String id() {
        return "APPLY_UPDATE_TRANSITION";
    }

    @Override
    public Result<Void, List<DomainError>> execute(UpdateQuestionnairePipelineContext context) {
        var existing = context.existingQuestionnaire();
        var command = context.command();
        var finalQuestions = context.configuredQuestions();
        var actor = context.updatedActor();

        String updatedDescription = resolveDescription(existing, command);
        if (updatedDescription == null || updatedDescription.isBlank()) {
            return QuestionnaireErrors.INVALID_DESCRIPTION.asFailure();
        }

        ParameterizationStatus desiredStatus = command.status() == null ? existing.status() : command.status();

        Questionnaire candidate = Questionnaire.rehydrate(
                existing.questionnaireId(),
                updatedDescription,
                existing.status(),
                finalQuestions,
                existing.auditInfo()
        );

        AuditInfo updatedAudit = existing.auditInfo().withUpdate(actor, command.updatedAt());
        QuestionnaireStatusTransitionContext transitionContext = new QuestionnaireStatusTransitionContext(
                candidate,
                actor,
                command.updatedAt(),
                updatedAudit,
                false
        );

        return QuestionnaireStatusMachine.transition(existing.status(), desiredStatus, transitionContext)
                .flatMap(transition -> Result.success(Questionnaire.rehydrate(
                        existing.questionnaireId(),
                        updatedDescription,
                        transition.targetState(),
                        finalQuestions,
                        transition.context().auditInfo()
                )))
                .map(questionnaire -> {
                    context.updatedQuestionnaire(questionnaire);
                    return null;
                });
    }

    private String resolveDescription(Questionnaire existing,
                                      com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateQuestionnaireCommand command) {
        return command.description() == null ? existing.description() : command.description();
    }
}

