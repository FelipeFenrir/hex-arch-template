package com.acme.orderquestionnaire.application.question.service.context;

import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.shared.pattern.pipeline.PipelineContext;

import java.util.List;

/**
 * Type-safe data bag for the DeleteQuestion pipeline.
 *
 * <ul>
 *   <li>{@link #id()} — the question ID, set at construction, never mutated</li>
 *   <li>{@link #question(Question)} / {@link #question()} — written by
 *       {@code FetchQuestionForDeleteStep}; read by {@code CheckQuestionNotInUseStep}
 *       and {@code DeleteQuestionStep}</li>
 *   <li>{@link #relatedQuestionnaireIds(List)} / {@link #relatedQuestionnaireIds()} — written by
 *       {@code CheckQuestionNotInUseStep} when blocking references are found; used by the
 *       batch loop to populate {@code DeleteQuestionFailureView.relatedQuestionnaireIds}</li>
 * </ul>
 *
 * <p>A private {@link ExistingQuestion} wrapper record avoids key collision with
 * other potential {@code Question}-typed entries in the context bag.
 */
public class DeleteQuestionPipelineContext extends PipelineContext {

    private final String id;

    public DeleteQuestionPipelineContext(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    // ── Question (fetched for deletion) ───────────────────────────────────

    public void question(Question question) {
        put(ExistingQuestion.class, new ExistingQuestion(question));
    }

    public Question question() {
        return get(ExistingQuestion.class).value();
    }

    // ── Related questionnaire ids (populated on QUESTION_IN_USE failure) ──

    public void relatedQuestionnaireIds(List<String> ids) {
        put(RelatedQuestionnaireIds.class, new RelatedQuestionnaireIds(ids));
    }

    public List<String> relatedQuestionnaireIds() {
        if (!has(RelatedQuestionnaireIds.class)) {
            return List.of();
        }
        return get(RelatedQuestionnaireIds.class).value();
    }

    public record ExistingQuestion(Question value) { }

    public record RelatedQuestionnaireIds(List<String> value) { }
}
