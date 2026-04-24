package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireUpdatedView;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.context.UpdateQuestionnairePipelineContext;
import com.acme.shared.pattern.pipeline.RollbackStyle;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;
import java.util.Objects;

/**
 * Persists the already-mutated {@link com.acme.orderquestionnaire.domain.questionnaire.Questionnaire}
 * entity produced by an earlier step in the update pipeline.
 *
 * <p><strong>Why {@link RollbackStyle#FRAMEWORK_TRANSACTION} is fixed (not injected):</strong>
 * Unlike the create pipeline, compensating an update would require storing a snapshot of the
 * entity's previous state and restoring it on failure — logic that is not implemented here.
 * Therefore, rollback is always delegated to the framework's transaction boundary ({@code @Transactional}).
 * If compensation-style rollback for updates is ever needed, the step must be extended to
 * capture the original state before calling {@code update()} and restore it in {@code rollback()}.
 */
public class PersistUpdatedQuestionnaireStep implements Step<UpdateQuestionnairePipelineContext> {

    private final QuestionnaireCommandOutPort questionnaireCommandOutPort;

    public PersistUpdatedQuestionnaireStep(QuestionnaireCommandOutPort questionnaireCommandOutPort) {
        this.questionnaireCommandOutPort = Objects.requireNonNull(questionnaireCommandOutPort,
                "questionnaireCommandOutPort must not be null");
    }

    @Override
    public String id() {
        return "PERSIST_UPDATED_QUESTIONNAIRE";
    }

    @Override
    public RollbackStyle rollbackStyle() {
        return RollbackStyle.FRAMEWORK_TRANSACTION;
    }

    @Override
    public Result<Void, List<DomainError>> execute(UpdateQuestionnairePipelineContext context) {
        return questionnaireCommandOutPort.update(context.updatedQuestionnaire())
                .map(questionnaire -> {
                    context.updatedQuestionnaire(questionnaire);
                    context.updatedView(QuestionnaireUpdatedView.from(questionnaire));
                    return null;
                });
    }
}

