package com.acme.orderquestionnaire.application.question.service.context;

import com.acme.orderquestionnaire.application.question.dto.command.CreateQuestionCommand;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionCreatedView;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.shared.pattern.pipeline.PipelineContext;
import com.acme.shared.vo.AuditInfo;

public class CreateQuestionPipelineContext extends PipelineContext {

    private final CreateQuestionCommand command;

    public CreateQuestionPipelineContext(CreateQuestionCommand command) {
        this.command = command;
    }

    public CreateQuestionCommand command() {
        return command;
    }

    public void auditInfo(AuditInfo auditInfo) {
        put(AuditInfo.class, auditInfo);
    }

    public AuditInfo auditInfo() {
        return get(AuditInfo.class);
    }

    public void question(Question question) {
        put(Question.class, question);
    }

    public Question question() {
        return get(Question.class);
    }

    public void createdView(QuestionCreatedView view) {
        put(QuestionCreatedView.class, view);
    }

    public QuestionCreatedView createdView() {
        return get(QuestionCreatedView.class);
    }
}

