package com.acme.orderquestionnaire.application.question.service.step;

import com.acme.orderquestionnaire.application.question.dto.view.QuestionUpdatedView;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.question.service.context.UpdateQuestionPipelineContext;
import com.acme.shared.pattern.pipeline.RollbackStyle;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;
import java.util.Objects;

/**
 * Persists the already-mutated {@link com.acme.orderquestionnaire.domain.question.Question}
 * entity produced by an earlier step in the update pipeline.
 *
 * <p><strong>Why {@link RollbackStyle#FRAMEWORK_TRANSACTION} is fixed (not injected):</strong>
 * Unlike the create pipeline, compensating an update would require storing a snapshot of the
 * entity's previous state and restoring it on failure — logic that is not implemented here.
 * Therefore, rollback is always delegated to the framework's transaction boundary ({@code @Transactional}).
 * If compensation-style rollback for updates is ever needed, the step must be extended to
 * capture the original state before calling {@code update()} and restore it in {@code rollback()}.
 */
public class PersistUpdatedQuestionStep implements Step<UpdateQuestionPipelineContext> {

    private final QuestionCommandOutPort questionCommandOutPort;

    public PersistUpdatedQuestionStep(QuestionCommandOutPort questionCommandOutPort) {
        this.questionCommandOutPort = Objects.requireNonNull(questionCommandOutPort,
                "questionCommandOutPort must not be null");
    }

    @Override
    public String id() {
        return "PERSIST_UPDATED_QUESTION";
    }

    @Override
    public RollbackStyle rollbackStyle() {
        return RollbackStyle.FRAMEWORK_TRANSACTION;
    }

    @Override
    public Result<Void, List<DomainError>> execute(UpdateQuestionPipelineContext context) {
        return questionCommandOutPort.update(context.updatedQuestion())
                .map(question -> {
                    context.updatedQuestion(question);
                    context.updatedView(QuestionUpdatedView.from(question));
                    return null;
                });
    }
}

