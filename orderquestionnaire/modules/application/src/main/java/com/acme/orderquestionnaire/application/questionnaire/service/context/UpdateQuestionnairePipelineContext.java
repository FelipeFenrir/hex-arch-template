package com.acme.orderquestionnaire.application.questionnaire.service.context;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireUpdatedView;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.shared.pattern.pipeline.PipelineContext;
import com.acme.shared.vo.AuditUser;

import java.util.List;

public class UpdateQuestionnairePipelineContext extends PipelineContext {

    private final UpdateQuestionnaireCommand command;

    public UpdateQuestionnairePipelineContext(UpdateQuestionnaireCommand command) {
        this.command = command;
    }

    public UpdateQuestionnaireCommand command() {
        return command;
    }

    public void updatedActor(AuditUser auditUser) {
        put(AuditUser.class, auditUser);
    }

    public AuditUser updatedActor() {
        return get(AuditUser.class);
    }

    public void existingQuestionnaire(Questionnaire questionnaire) {
        put(ExistingQuestionnaire.class, new ExistingQuestionnaire(questionnaire));
    }

    public Questionnaire existingQuestionnaire() {
        return get(ExistingQuestionnaire.class).value();
    }

    public void configuredQuestions(List<ConfiguredQuestion> configuredQuestions) {
        put(ResolvedConfiguredQuestions.class, new ResolvedConfiguredQuestions(configuredQuestions));
    }

    public List<ConfiguredQuestion> configuredQuestions() {
        return get(ResolvedConfiguredQuestions.class).value();
    }

    public void updatedQuestionnaire(Questionnaire questionnaire) {
        put(UpdatedQuestionnaire.class, new UpdatedQuestionnaire(questionnaire));
    }

    public Questionnaire updatedQuestionnaire() {
        return get(UpdatedQuestionnaire.class).value();
    }

    public void updatedView(QuestionnaireUpdatedView view) {
        put(QuestionnaireUpdatedView.class, view);
    }

    public QuestionnaireUpdatedView updatedView() {
        return get(QuestionnaireUpdatedView.class);
    }

    public record ExistingQuestionnaire(Questionnaire value) { }

    public record UpdatedQuestionnaire(Questionnaire value) { }

    public record ResolvedConfiguredQuestions(List<ConfiguredQuestion> value) { }
}

