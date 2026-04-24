package com.acme.orderquestionnaire.application.questionnaire.service.context;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.DeleteQuestionnaireCommand;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.shared.pattern.pipeline.PipelineContext;

/**
 * Type-safe data bag for the DeleteQuestionnaire pipeline.
 *
 * <ul>
 *   <li>{@link #command()} — input, set at construction, never mutated</li>
 *   <li>{@link #questionnaire(Questionnaire)} / {@link #questionnaire()} — written by
 *       {@code FetchQuestionnaireForDeleteStep}; read by {@code ValidateQuestionnaireDeleteEligibilityStep}
 *       and {@code DeleteQuestionnaireStep}</li>
 * </ul>
 *
 * <p>A private {@link ExistingQuestionnaire} wrapper record avoids key collision when
 * a future step also places a {@code Questionnaire} under a different semantic key.
 */
public class DeleteQuestionnairePipelineContext extends PipelineContext {

    private final DeleteQuestionnaireCommand command;

    public DeleteQuestionnairePipelineContext(DeleteQuestionnaireCommand command) {
        this.command = command;
    }

    public DeleteQuestionnaireCommand command() {
        return command;
    }

    // ── Questionnaire (fetched for deletion) ──────────────────────────────

    public void questionnaire(Questionnaire questionnaire) {
        put(ExistingQuestionnaire.class, new ExistingQuestionnaire(questionnaire));
    }

    public Questionnaire questionnaire() {
        return get(ExistingQuestionnaire.class).value();
    }

    public record ExistingQuestionnaire(Questionnaire value) { }
}

