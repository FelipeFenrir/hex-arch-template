package com.acme.orderquestionnaire.application.question.service.context;

import com.acme.orderquestionnaire.application.question.dto.command.UpdateQuestionCommand;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionUpdatedView;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.shared.pattern.pipeline.PipelineContext;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.AuditUser;
import com.acme.shared.vo.QuestionId;

public class UpdateQuestionPipelineContext extends PipelineContext {

    private final String id;
    private final UpdateQuestionCommand command;

    public UpdateQuestionPipelineContext(String id, UpdateQuestionCommand command) {
        this.id = id;
        this.command = command;
    }

    public String id() {
        return id;
    }

    public QuestionId questionId() {
        return QuestionId.of(id);
    }

    public UpdateQuestionCommand command() {
        return command;
    }

    public void updatedActor(AuditUser auditUser) {
        put(AuditUser.class, auditUser);
    }

    public AuditUser updatedActor() {
        return get(AuditUser.class);
    }

    public void existingQuestion(Question question) {
        put(ExistingQuestion.class, new ExistingQuestion(question));
    }

    public Question existingQuestion() {
        return get(ExistingQuestion.class).value();
    }

    public void transitionData(TransitionData transitionData) {
        put(TransitionData.class, transitionData);
    }

    public TransitionData transitionData() {
        return get(TransitionData.class);
    }

    public void updatedQuestion(Question question) {
        put(UpdatedQuestion.class, new UpdatedQuestion(question));
    }

    public Question updatedQuestion() {
        return get(UpdatedQuestion.class).value();
    }

    public void updatedView(QuestionUpdatedView view) {
        put(QuestionUpdatedView.class, view);
    }

    public QuestionUpdatedView updatedView() {
        return get(QuestionUpdatedView.class);
    }

    public record ExistingQuestion(Question value) { }

    public record UpdatedQuestion(Question value) { }

    public record TransitionData(ParameterizationStatus targetState, AuditInfo auditInfo) { }
}

