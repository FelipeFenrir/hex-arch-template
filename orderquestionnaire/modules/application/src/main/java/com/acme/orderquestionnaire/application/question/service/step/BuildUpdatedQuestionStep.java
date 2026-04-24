package com.acme.orderquestionnaire.application.question.service.step;

import com.acme.orderquestionnaire.application.question.service.context.UpdateQuestionPipelineContext;
import com.acme.orderquestionnaire.domain.question.QuestionFactory;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;

public class BuildUpdatedQuestionStep implements Step<UpdateQuestionPipelineContext> {

    @Override
    public String id() {
        return "BUILD_UPDATED_QUESTION";
    }

    @Override
    public Result<Void, List<DomainError>> execute(UpdateQuestionPipelineContext context) {
        var command = context.command();
        var transition = context.transitionData();

        return QuestionFactory.rehydrate(
                        context.id(),
                        command.label(),
                        transition.targetState(),
                        command.salesItemReferenceCode(),
                        transition.auditInfo()
                )
                .flatMap(builder -> builder.build())
                .map(question -> {
                    context.updatedQuestion(question);
                    return null;
                });
    }
}

