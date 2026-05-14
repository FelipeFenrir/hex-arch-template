package com.acme.orderquestionnaire.application.question.service.step;

import com.acme.orderquestionnaire.application.question.dto.view.QuestionCreatedView;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.question.service.context.CreateQuestionPipelineContext;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.question.QuestionFactory;
import com.acme.shared.pattern.pipeline.RollbackStyle;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.List;
import java.util.Objects;

public class BuildAndPersistQuestionStep implements Step<CreateQuestionPipelineContext> {

    private final QuestionCommandOutPort questionCommandOutPort;
    private final RollbackStyle rollbackStyle;

    public BuildAndPersistQuestionStep(QuestionCommandOutPort questionCommandOutPort,
                                       RollbackStyle rollbackStyle) {
        this.questionCommandOutPort = Objects.requireNonNull(questionCommandOutPort,
                "questionCommandOutPort must not be null");
        this.rollbackStyle = Objects.requireNonNull(rollbackStyle,
                "rollbackStyle must not be null");
    }

    @Override
    public String id() {
        return "BUILD_AND_PERSIST_QUESTION";
    }

    @Override
    public RollbackStyle rollbackStyle() {
        return rollbackStyle;
    }

    @Override
    public Result<Void, List<DomainError>> execute(CreateQuestionPipelineContext context) {
        var command = context.command();

        return QuestionFactory.createNew(
                        command.questionId(),
                        command.label(),
                        command.salesItemCode(),
                        context.auditInfo()
                )
                .flatMap(builder -> builder.build())
                .flatMap(questionCommandOutPort::create)
                .map(question -> {
                    context.question(question);
                    context.createdView(QuestionCreatedView.from(question));
                    return (Void) null;
                });
    }

    @Override
    public void rollback(CreateQuestionPipelineContext context) {
        if (rollbackStyle != RollbackStyle.COMPENSATION) {
            return;
        }
        if (!context.has(Question.class)) {
            return;
        }
        questionCommandOutPort.deleteById(context.question().id());
    }
}

