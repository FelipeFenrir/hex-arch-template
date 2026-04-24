package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireCreatedView;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.context.CreateQuestionnairePipelineContext;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireFactory;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.pattern.pipeline.RollbackStyle;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;
import java.util.Objects;

/**
 * Builds the {@link Questionnaire} domain entity via {@link QuestionnaireFactory} and persists it.
 *
 * <p>This is the only step with an observable side-effect (database write), so it supports
 * both rollback strategies via the injected {@link RollbackStyle}:
 *
 * <ul>
 *   <li>{@link RollbackStyle#COMPENSATION} — the orchestrator calls {@link #rollback(CreateQuestionnairePipelineContext)}
 *       which deletes the persisted record. Use this when there is no enclosing {@code @Transactional}.</li>
 *   <li>{@link RollbackStyle#FRAMEWORK_TRANSACTION} — rollback is delegated to the framework;
 *       {@link #rollback(CreateQuestionnairePipelineContext)} becomes a no-op. Use this when the
 *       caller wraps the pipeline in a {@code @Transactional} boundary.</li>
 * </ul>
 *
 * <p>On success, writes both the persisted {@code Questionnaire} and the
 * {@link QuestionnaireCreatedView} into the context.
 */
public class BuildAndPersistQuestionnaireStep implements Step<CreateQuestionnairePipelineContext> {

    private final QuestionnaireCommandOutPort questionnaireCommandOutPort;
    private final RollbackStyle rollbackStyle;

    public BuildAndPersistQuestionnaireStep(QuestionnaireCommandOutPort questionnaireCommandOutPort,
                                            RollbackStyle rollbackStyle) {
        this.questionnaireCommandOutPort = Objects.requireNonNull(questionnaireCommandOutPort,
                "questionnaireCommandOutPort must not be null");
        this.rollbackStyle = Objects.requireNonNull(rollbackStyle,
                "rollbackStyle must not be null");
    }

    @Override
    public String id() {
        return "BUILD_AND_PERSIST_QUESTIONNAIRE";
    }

    @Override
    public RollbackStyle rollbackStyle() {
        return rollbackStyle;
    }

    @Override
    public Result<Void, List<DomainError>> execute(CreateQuestionnairePipelineContext context) {
        var command = context.command();

        return QuestionnaireFactory.createNew(
                        command.id(),
                        command.channelDistributionId(),
                        command.journeyDistributionId(),
                        command.description(),
                        context.auditInfo()
                )
                .flatMap(builder -> builder.build())
                .flatMap(questionnaireCommandOutPort::create)
                .map(questionnaire -> {
                    context.questionnaire(questionnaire);
                    context.createdView(QuestionnaireCreatedView.from(questionnaire));
                    return (Void) null;
                });
    }

    /**
     * Compensation rollback: deletes the questionnaire created by {@link #execute}.
     * Invoked by the orchestrator in LIFO order only when {@code rollbackStyle == COMPENSATION}.
     * Safe to call even if the questionnaire was never written (guards on context presence).
     */
    @Override
    public void rollback(CreateQuestionnairePipelineContext context) {
        if (rollbackStyle != RollbackStyle.COMPENSATION) {
            return;
        }
        if (!context.has(Questionnaire.class)) {
            return;
        }
        Questionnaire questionnaire = context.questionnaire();
        QuestionnaireId questionnaireId = QuestionnaireId.of(
                questionnaire.id(),
                questionnaire.channelDistributionId(),
                questionnaire.journeyDistributionId()
        );
        questionnaireCommandOutPort.deleteById(questionnaireId);
    }
}

