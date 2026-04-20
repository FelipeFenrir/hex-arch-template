package com.acme.orderquestionnaire.domain.question;

import com.acme.shared.engine.state.StateMachineConfig;
import com.acme.shared.engine.state.TransitionResult;
import com.acme.shared.engine.state.TransitionAction;
import com.acme.shared.engine.rule.GenericRule;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;

public final class QuestionStatusMachine {

    public static final StateMachineConfig<ParameterizationStatus, QuestionStatusTransitionContext> INSTANCE =
            new StateMachineConfig<ParameterizationStatus, QuestionStatusTransitionContext>()
                    .addTransition(
                            ParameterizationStatus.DRAFT,
                            ParameterizationStatus.ACTIVE,
                            List.of(requiredTransitionMetadata()),
                            List.of(markStatusChanged(), updateAuditInfo())
                    )
                    .addTransition(
                            ParameterizationStatus.INACTIVE,
                            ParameterizationStatus.ACTIVE,
                            List.of(requiredTransitionMetadata()),
                            List.of(markStatusChanged(), updateAuditInfo())
                    )
                    .addTransition(
                            ParameterizationStatus.ACTIVE,
                            ParameterizationStatus.INACTIVE,
                            List.of(requiredTransitionMetadata()),
                            List.of(markStatusChanged(), updateAuditInfo())
                    );

    public static final String ACTOR_AND_OCCURRED_AT_MUST_BE_PROVIDED = "actor and occurredAt must be provided";

    private QuestionStatusMachine() {
        throw new IllegalStateException("Utility class");
    }

    private static GenericRule<QuestionStatusTransitionContext> requiredTransitionMetadata() {
        return new GenericRule<>(
                ctx -> ctx.actor() != null && ctx.occurredAt() != null,
                ACTOR_AND_OCCURRED_AT_MUST_BE_PROVIDED
        );
    }

    private static TransitionAction<QuestionStatusTransitionContext> markStatusChanged() {
        return ctx -> Result.success(ctx.withStatusChanged(true));
    }

    private static TransitionAction<QuestionStatusTransitionContext> updateAuditInfo() {
        return ctx -> Result.success(ctx.withAuditInfo(ctx.auditInfo().withUpdate(ctx.actor(), ctx.occurredAt())));
    }

    public static Result<ParameterizationStatus, List<DomainError>> validateTransition(ParameterizationStatus current,
                                                                                         ParameterizationStatus desired,
                                                                                         QuestionStatusTransitionContext context) {
        return transition(current, desired, context)
                .map(transition -> transition.targetState());
    }

    public static Result<TransitionResult<ParameterizationStatus, QuestionStatusTransitionContext>, List<DomainError>> transition(
            ParameterizationStatus current,
            ParameterizationStatus desired,
            QuestionStatusTransitionContext context) {
        return INSTANCE.transition(current, desired, context);
    }
}


