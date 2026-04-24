package com.acme.orderquestionnaire.application.questionnaire.service.context;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.ValidateQuestionnaireAnswersCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.ValidateQuestionnaireAnswersView;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.shared.pattern.pipeline.PipelineContext;

/**
 * Type-safe Data Bag for the ValidateQuestionnaireAnswers pipeline.
 */
public class ValidateQuestionnaireAnswersPipelineContext extends PipelineContext {

    private final ValidateQuestionnaireAnswersCommand command;

    public ValidateQuestionnaireAnswersPipelineContext(ValidateQuestionnaireAnswersCommand command) {
        this.command = command;
    }

    public ValidateQuestionnaireAnswersCommand command() {
        return command;
    }

    public void questionnaire(Questionnaire questionnaire) {
        put(ResolvedQuestionnaire.class, new ResolvedQuestionnaire(questionnaire));
    }

    public Questionnaire questionnaire() {
        return get(ResolvedQuestionnaire.class).value();
    }

    public void validationView(ValidateQuestionnaireAnswersView view) {
        put(ValidateQuestionnaireAnswersView.class, view);
    }

    public ValidateQuestionnaireAnswersView validationView() {
        return get(ValidateQuestionnaireAnswersView.class);
    }

    public record ResolvedQuestionnaire(Questionnaire value) { }
}

