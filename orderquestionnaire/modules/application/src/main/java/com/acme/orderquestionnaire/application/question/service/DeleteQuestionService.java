package com.acme.orderquestionnaire.application.question.service;

import com.acme.orderquestionnaire.application.question.dto.view.DeleteQuestionFailureView;
import com.acme.orderquestionnaire.application.question.dto.view.DeleteQuestionsResultView;
import com.acme.orderquestionnaire.application.question.error.QuestionErrors;
import com.acme.orderquestionnaire.application.question.port.in.usecase.DeleteQuestionUseCase;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Guard;
import com.acme.shared.pattern.result.Result;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DeleteQuestionService implements DeleteQuestionUseCase {

    private final QuestionCommandOutPort questionRepository;
    private final QuestionnaireCommandOutPort questionnaireRepository;

    public DeleteQuestionService(QuestionCommandOutPort questionRepository,
                                 QuestionnaireCommandOutPort questionnaireRepository) {
        this.questionRepository = Objects.requireNonNull(questionRepository, "questionRepository must not be null");
        this.questionnaireRepository = Objects.requireNonNull(questionnaireRepository,
                "questionnaireRepository must not be null");
    }

    @Override
    public Result<Void, List<DomainError>> execute(String id) {
        Result<Void, List<DomainError>> idValidation = Guard.requireNonBlank(id, QuestionErrors.INVALID_ID);
        if (idValidation.isFailure()) {
            return Result.failure(idValidation.errorOrElseThrow(() ->
                    new IllegalStateException("Expected failure idValidation result")));
        }

        if (questionRepository.findQuestionById(id).isEmpty()) {
            return QuestionErrors.QUESTION_NOT_FOUND.asFailure();
        }

        List<String> relatedQuestionnaireIds = relatedQuestionnaireIds(id, findReferences(List.of(id)));
        if (!relatedQuestionnaireIds.isEmpty()) {
            return QuestionErrors.QUESTION_IN_USE.asFailure(id, String.join(",", relatedQuestionnaireIds));
        }

        return questionRepository.deleteById(id).flatMap(__ -> Result.success(null));
    }

    @Override
    public Result<DeleteQuestionsResultView, List<DomainError>> execute(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return QuestionErrors.INVALID_IDS.asFailure();
        }

        List<String> validIds = ids.stream()
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .distinct()
                .toList();

        Map<String, List<String>> referencesByQuestionId = findReferences(validIds);
        List<DeleteQuestionFailureView> failures = new ArrayList<>();

        for (String id : ids) {
            Optional<DeleteQuestionFailureView> failure = deleteOneAndCollectFailure(id, referencesByQuestionId);
            failure.ifPresent(failures::add);
        }

        return Result.success(new DeleteQuestionsResultView(failures));
    }

    private Optional<DeleteQuestionFailureView> deleteOneAndCollectFailure(String id,
                                                                           Map<String, List<String>> referencesByQuestionId) {
        if (id == null || id.isBlank()) {
            DomainError error = QuestionErrors.INVALID_ID.toDomainError();
            return Optional.of(new DeleteQuestionFailureView(id, error.code(), error.message(), List.of()));
        }

        if (questionRepository.findQuestionById(id).isEmpty()) {
            DomainError error = QuestionErrors.QUESTION_NOT_FOUND.toDomainError();
            return Optional.of(new DeleteQuestionFailureView(id, error.code(), error.message(), List.of()));
        }

        List<String> relatedQuestionnaireIds = relatedQuestionnaireIds(id, referencesByQuestionId);
        if (!relatedQuestionnaireIds.isEmpty()) {
            DomainError error = QuestionErrors.QUESTION_IN_USE.toDomainError(id, String.join(",", relatedQuestionnaireIds));
            return Optional.of(new DeleteQuestionFailureView(id, error.code(), error.message(), relatedQuestionnaireIds));
        }

        Result<Void, List<DomainError>> deleteResult = questionRepository.deleteById(id);
        if (deleteResult.isFailure()) {
            List<DomainError> errors = deleteResult.errorOrElseThrow(() ->
                    new IllegalStateException("Expected delete failure result"));

            DomainError firstError = errors.isEmpty()
                    ? QuestionErrors.QUESTION_DELETE_FAILED.toDomainError(id, "unknown reason")
                    : errors.getFirst();

            return Optional.of(new DeleteQuestionFailureView(id, firstError.code(), firstError.message(), List.of()));
        }

        return Optional.empty();
    }

    private Map<String, List<String>> findReferences(List<String> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Map.of();
        }

        Map<String, List<String>> references = questionnaireRepository
                .findReferencingQuestionnaireIdsByQuestionIds(questionIds);
        return references == null ? Map.of() : references;
    }

    private List<String> relatedQuestionnaireIds(String questionId, Map<String, List<String>> referencesByQuestionId) {
        return referencesByQuestionId.getOrDefault(questionId, List.of()).stream()
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                        list -> list.stream().sorted(Comparator.naturalOrder()).toList()
                ));
    }
}

