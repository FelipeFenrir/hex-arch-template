package com.acme.orderquestionnaire.domain.questionnaire;

import com.acme.orderquestionnaire.domain.questionnaire.errors.QuestionnaireDomainErrors;
import com.acme.shared.engine.rule.GenericRule;
import com.acme.shared.engine.state.StateMachineConfig;
import com.acme.shared.engine.state.TransitionAction;
import com.acme.shared.engine.state.TransitionResult;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;

public final class QuestionnaireStatusMachine {

    public static final String ACTOR_AND_OCCURRED_AT_MUST_BE_PROVIDED = "actor and occurredAt must be provided";

    public static final StateMachineConfig<ParameterizationStatus, QuestionnaireStatusTransitionContext> INSTANCE =
            new StateMachineConfig<ParameterizationStatus, QuestionnaireStatusTransitionContext>()
                    .addTransition(
                            ParameterizationStatus.DRAFT,
                            ParameterizationStatus.ACTIVE,
                            List.of(requiredTransitionMetadata()),
                            List.of(validateActivationReadiness(), markStatusChanged(), updateAuditInfo())
                    )
                    .addTransition(
                            ParameterizationStatus.INACTIVE,
                            ParameterizationStatus.ACTIVE,
                            List.of(requiredTransitionMetadata()),
                            List.of(validateActivationReadiness(), markStatusChanged(), updateAuditInfo())
                    )
                    .addTransition(
                            ParameterizationStatus.DRAFT,
                            ParameterizationStatus.INACTIVE,
                            List.of(requiredTransitionMetadata()),
                            List.of(markStatusChanged(), updateAuditInfo())
                    )
                    .addTransition(
                            ParameterizationStatus.ACTIVE,
                            ParameterizationStatus.INACTIVE,
                            List.of(requiredTransitionMetadata()),
                            List.of(markStatusChanged(), updateAuditInfo())
                    );

    private QuestionnaireStatusMachine() {
        throw new IllegalStateException("Utility class");
    }

    private static GenericRule<QuestionnaireStatusTransitionContext> requiredTransitionMetadata() {
        return new GenericRule<>(
                ctx -> ctx.actor() != null && ctx.occurredAt() != null,
                ACTOR_AND_OCCURRED_AT_MUST_BE_PROVIDED
        );
    }

    private static TransitionAction<QuestionnaireStatusTransitionContext> validateActivationReadiness() {
        return ctx -> {
            if (ctx.questionnaire() == null || !ctx.questionnaire().isReadyToActivate()) {
                return Result.failure(List.of(QuestionnaireDomainErrors.invalidConfiguredQuestionForActivation()));
            }
            return Result.success(ctx);
        };
    }

    private static TransitionAction<QuestionnaireStatusTransitionContext> markStatusChanged() {
        return ctx -> Result.success(ctx.withStatusChanged(true));
    }

    private static TransitionAction<QuestionnaireStatusTransitionContext> updateAuditInfo() {
        return ctx -> Result.success(ctx.withAuditInfo(ctx.auditInfo().withUpdate(ctx.actor(), ctx.occurredAt())));
    }

    public static Result<ParameterizationStatus, List<DomainError>> validateTransition(
            ParameterizationStatus current,
            ParameterizationStatus desired,
            QuestionnaireStatusTransitionContext context) {
        return transition(current, desired, context).map(TransitionResult::targetState);
    }

    public static Result<TransitionResult<ParameterizationStatus, QuestionnaireStatusTransitionContext>, List<DomainError>> transition(
            ParameterizationStatus current,
            ParameterizationStatus desired,
            QuestionnaireStatusTransitionContext context) {
        return INSTANCE.transition(current, desired, context);
    }
}

