package com.acme.orderquestionnaire.application.question.service.step;

import com.acme.orderquestionnaire.application.question.service.context.UpdateQuestionPipelineContext;
import com.acme.orderquestionnaire.domain.question.QuestionStatusMachine;
import com.acme.orderquestionnaire.domain.question.QuestionStatusTransitionContext;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;

public class ResolveQuestionTransitionStep implements Step<UpdateQuestionPipelineContext> {

    @Override
    public String id() {
        return "RESOLVE_TRANSITION";
    }

    @Override
    public Result<Void, List<DomainError>> execute(UpdateQuestionPipelineContext context) {
        var current = context.existingQuestion();
        var command = context.command();
        var actor = context.updatedActor();

        QuestionStatusTransitionContext transitionContext = new QuestionStatusTransitionContext(
                current,
                actor,
                command.updatedAt(),
                current.auditInfo().withUpdate(actor, command.updatedAt()),
                false
        );

        ParameterizationStatus desiredStatus = command.status() == null
                ? current.status()
                : command.status();

        return QuestionStatusMachine.transition(current.status(), desiredStatus, transitionContext)
                .map(transition -> {
                    context.transitionData(new UpdateQuestionPipelineContext.TransitionData(
                            transition.targetState(),
                            transition.context().auditInfo()
                    ));
                    return null;
                });
    }
}

