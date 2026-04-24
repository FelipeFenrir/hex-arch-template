package com.acme.orderquestionnaire.application.questionnaire.service.context;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.CreateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireCreatedView;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.shared.pattern.pipeline.PipelineContext;
import com.acme.shared.vo.AuditInfo;

/**
 * Type-safe Data Bag for the CreateQuestionnaire pipeline.
 *
 * <p>Exposes strongly-typed accessors instead of the raw {@code get/put} API
 * so that each Step reads and writes named, domain-meaningful values:
 * <ul>
 *   <li>{@link #command()} — input, set at construction, never mutated</li>
 *   <li>{@link #auditInfo(AuditInfo)} / {@link #auditInfo()} — written by {@code BuildAuditStep}</li>
 *   <li>{@link #questionnaire(Questionnaire)} / {@link #questionnaire()} — written by {@code BuildAndPersistQuestionnaireStep}; also used by rollback</li>
 *   <li>{@link #createdView(QuestionnaireCreatedView)} / {@link #createdView()} — written by {@code BuildAndPersistQuestionnaireStep}; extracted by the orchestrator on success</li>
 * </ul>
 */
public class CreateQuestionnairePipelineContext extends PipelineContext {

    private final CreateQuestionnaireCommand command;

    public CreateQuestionnairePipelineContext(CreateQuestionnaireCommand command) {
        this.command = command;
    }

    public CreateQuestionnaireCommand command() {
        return command;
    }

    // ── AuditInfo ─────────────────────────────────────────────────────────

    public void auditInfo(AuditInfo auditInfo) {
        put(AuditInfo.class, auditInfo);
    }

    public AuditInfo auditInfo() {
        return get(AuditInfo.class);
    }

    // ── Questionnaire (domain entity) ─────────────────────────────────────

    public void questionnaire(Questionnaire questionnaire) {
        put(Questionnaire.class, questionnaire);
    }

    public Questionnaire questionnaire() {
        return get(Questionnaire.class);
    }

    // ── QuestionnaireCreatedView (output) ─────────────────────────────────

    public void createdView(QuestionnaireCreatedView view) {
        put(QuestionnaireCreatedView.class, view);
    }

    public QuestionnaireCreatedView createdView() {
        return get(QuestionnaireCreatedView.class);
    }
}

