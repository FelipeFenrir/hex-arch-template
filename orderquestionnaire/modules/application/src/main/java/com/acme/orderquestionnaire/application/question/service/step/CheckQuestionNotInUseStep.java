package com.acme.orderquestionnaire.application.question.service.step;

import com.acme.orderquestionnaire.application.question.error.QuestionErrors;
import com.acme.orderquestionnaire.application.question.service.context.DeleteQuestionPipelineContext;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Ensures the question is not referenced by any active questionnaire before deletion.
 *
 * <p>Calls {@code findReferencingQuestionnaireIdsByQuestionIds} with the single question ID
 * and fails with {@code QUESTION_IN_USE} — listing all referencing questionnaire IDs sorted —
 * if any references exist.
 *
 * <p>No side-effects; rollback is a no-op (default {@code NONE}).
 */
public class CheckQuestionNotInUseStep implements Step<DeleteQuestionPipelineContext> {

    private final QuestionnaireCommandOutPort questionnaireCommandOutPort;

    public CheckQuestionNotInUseStep(QuestionnaireCommandOutPort questionnaireCommandOutPort) {
        this.questionnaireCommandOutPort = Objects.requireNonNull(questionnaireCommandOutPort,
                "questionnaireCommandOutPort must not be null");
    }

    @Override
    public String id() {
        return "CHECK_NOT_IN_USE";
    }

    @Override
    public Result<Void, List<DomainError>> execute(DeleteQuestionPipelineContext context) {
        String questionId = context.id();

        Map<String, List<String>> references = questionnaireCommandOutPort
                .findReferencingQuestionnaireIdsByQuestionIds(List.of(questionId));

        List<String> relatedIds = sortedUnique(
                references == null ? List.of() : references.getOrDefault(questionId, List.of())
        );

        if (!relatedIds.isEmpty()) {
            context.relatedQuestionnaireIds(relatedIds);
            return QuestionErrors.QUESTION_IN_USE.asFailure(questionId, String.join(",", relatedIds));
        }

        return Result.success(null);
    }

    private static List<String> sortedUnique(List<String> ids) {
        return ids.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new),
                        set -> set.stream().sorted(Comparator.naturalOrder()).toList()
                ));
    }
}


