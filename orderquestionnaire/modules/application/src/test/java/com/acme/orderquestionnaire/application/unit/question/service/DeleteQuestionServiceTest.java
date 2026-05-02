package com.acme.orderquestionnaire.application.unit.question.service;

import com.acme.orderquestionnaire.application.question.dto.view.DeleteQuestionsResultView;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.question.service.DeleteQuestionService;
import com.acme.orderquestionnaire.application.question.service.context.DeleteQuestionPipelineContext;
import com.acme.orderquestionnaire.application.question.service.step.CheckQuestionNotInUseStep;
import com.acme.orderquestionnaire.application.question.service.step.DeleteQuestionStep;
import com.acme.orderquestionnaire.application.question.service.step.FetchQuestionForDeleteStep;
import com.acme.orderquestionnaire.application.question.service.step.ValidateDeleteQuestionIdStep;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.Id;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@DisplayName("DeleteQuestionService")
class DeleteQuestionServiceTest {

    private QuestionCommandOutPort questionRepository;
    private QuestionnaireCommandOutPort questionnaireRepository;
    private DeleteQuestionService service;

    @BeforeEach
    void setUp() {
        questionRepository = mock(QuestionCommandOutPort.class);
        questionnaireRepository = mock(QuestionnaireCommandOutPort.class);
        service = buildService(questionRepository, questionnaireRepository);

        when(questionnaireRepository.findReferencingQuestionnaireIdsByQuestionIds(anyList())).thenReturn(Map.of());
        when(questionRepository.deleteById(any())).thenReturn(Result.success(null));
    }

    // ── Constructor / step guards ─────────────────────────────────────────────

    @Test
    @DisplayName("should throw when steps list is null")
    void shouldThrowWhenStepsListIsNull() {
        assertThrows(NullPointerException.class, () -> new DeleteQuestionService(null));
    }

    @Test
    @DisplayName("should throw when questionCommandOutPort is null inside FetchQuestionForDeleteStep")
    void shouldThrowWhenPortIsNullInFetchStep() {
        assertThrows(NullPointerException.class, () -> new FetchQuestionForDeleteStep(null));
    }

    @Test
    @DisplayName("should throw when questionnaireCommandOutPort is null inside CheckQuestionNotInUseStep")
    void shouldThrowWhenPortIsNullInCheckStep() {
        assertThrows(NullPointerException.class, () -> new CheckQuestionNotInUseStep(null));
    }

    @Test
    @DisplayName("should throw when questionCommandOutPort is null inside DeleteQuestionStep")
    void shouldThrowWhenPortIsNullInDeleteStep() {
        assertThrows(NullPointerException.class, () -> new DeleteQuestionStep(null));
    }

    // ── Single delete ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("execute(id): should delete question when it exists and has no questionnaire references")
    void shouldDeleteWhenQuestionExistsAndHasNoReferences() {
        when(questionRepository.findQuestionById("q_001")).thenReturn(Optional.of(question("q_001")));

        Result<Void, List<DomainError>> result = service.execute("q_001");

        assertInstanceOf(Result.Success.class, result);
        verify(questionnaireRepository).findReferencingQuestionnaireIdsByQuestionIds(List.of("q_001"));
        verify(questionRepository).deleteById("q_001");
    }

    @Test
    @DisplayName("execute(id): should fail when question is not found")
    void shouldFailWhenQuestionIsNotFound() {
        when(questionRepository.findQuestionById("q_missing")).thenReturn(Optional.empty());

        Result<Void, List<DomainError>> result = service.execute("q_missing");

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("QUESTION_NOT_FOUND")));
        verify(questionRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("execute(id): should fail when question has questionnaire references")
    void shouldFailWhenQuestionHasQuestionnaireReferences() {
        when(questionRepository.findQuestionById("q_002")).thenReturn(Optional.of(question("q_002")));
        when(questionnaireRepository.findReferencingQuestionnaireIdsByQuestionIds(List.of("q_002")))
                .thenReturn(Map.of("q_002", List.of("qst_2", "qst_1")));

        Result<Void, List<DomainError>> result = service.execute("q_002");

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("QUESTION_IN_USE")));
        assertTrue(errors.stream().anyMatch(error -> error.message().contains("qst_1,qst_2")));
        verify(questionRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("execute(id): QUESTION_IN_USE message should contain sorted questionnaire ids and question id (snapshot)")
    void singleDeleteInUseErrorMessageSnapshot() {
        when(questionRepository.findQuestionById("q_abc")).thenReturn(Optional.of(question("q_abc")));
        when(questionnaireRepository.findReferencingQuestionnaireIdsByQuestionIds(List.of("q_abc")))
                .thenReturn(Map.of("q_abc", List.of("qst_z", "qst_a", "qst_m")));

        Result<Void, List<DomainError>> result = service.execute("q_abc");

        assertInstanceOf(Result.Failure.class, result);
        DomainError error = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure")).getFirst();

        assertEquals("QUESTION_IN_USE", error.code());
        assertEquals("question 'q_abc' is referenced by questionnaires: qst_a,qst_m,qst_z", error.message());
        verify(questionRepository, never()).deleteById(any());
    }

    // ── Batch delete ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("execute(ids): should process full batch and return only per-question failures")
    void shouldProcessBatchAndReturnOnlyFailures() {
        when(questionRepository.findQuestionById("q_ok")).thenReturn(Optional.of(question("q_ok")));
        when(questionRepository.findQuestionById("q_in_use")).thenReturn(Optional.of(question("q_in_use")));
        when(questionRepository.findQuestionById("q_missing")).thenReturn(Optional.empty());

        when(questionnaireRepository.findReferencingQuestionnaireIdsByQuestionIds(List.of("q_in_use")))
                .thenReturn(Map.of("q_in_use", List.of("qst_9")));

        Result<DeleteQuestionsResultView, List<DomainError>> result = service.execute(List.of("q_ok", "q_in_use", "q_missing"));

        assertInstanceOf(Result.Success.class, result);
        DeleteQuestionsResultView view = result.getOrElseThrow(errors ->
                new IllegalStateException("Expected success but got failure: " + errors));

        assertEquals(2, view.failures().size());
        assertTrue(view.failures().stream().anyMatch(failure ->
                failure.questionId().equals("q_in_use")
                        && failure.code().equals("QUESTION_IN_USE")
                        && failure.relatedQuestionnaireIds().contains("qst_9")));
        assertTrue(view.failures().stream().anyMatch(failure ->
                failure.questionId().equals("q_missing") && failure.code().equals("QUESTION_NOT_FOUND")));

        verify(questionRepository).deleteById("q_ok");
        verify(questionRepository, never()).deleteById("q_in_use");
    }

    @Test
    @DisplayName("execute(ids): batch failure view should expose structured relatedQuestionnaireIds sorted (snapshot)")
    void batchShouldPopulateRelatedIdsListInFailureView() {
        when(questionRepository.findQuestionById("q_x")).thenReturn(Optional.of(question("q_x")));
        when(questionnaireRepository.findReferencingQuestionnaireIdsByQuestionIds(List.of("q_x")))
                .thenReturn(Map.of("q_x", List.of("qst_z", "qst_a")));

        DeleteQuestionsResultView view = service.execute(List.of("q_x"))
                .getOrElseThrow(e -> new IllegalStateException("Expected success but got failure: " + e));

        assertEquals(1, view.failures().size());
        var failure = view.failures().getFirst();
        assertEquals("q_x", failure.questionId());
        assertEquals("QUESTION_IN_USE", failure.code());
        assertEquals(List.of("qst_a", "qst_z"), failure.relatedQuestionnaireIds());
        verify(questionRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("execute(ids): should fail globally when ids list is null or empty")
    void shouldFailGloballyWhenIdsListIsInvalid() {
        Result<DeleteQuestionsResultView, List<DomainError>> nullResult = service.execute((List<String>) null);
        Result<DeleteQuestionsResultView, List<DomainError>> emptyResult = service.execute(List.of());

        assertInstanceOf(Result.Failure.class, nullResult);
        assertInstanceOf(Result.Failure.class, emptyResult);
    }

    // ── Factory helpers ───────────────────────────────────────────────────────

    public static DeleteQuestionService buildService(QuestionCommandOutPort questionRepo,
                                                     QuestionnaireCommandOutPort questionnaireRepo) {
        List<Step<DeleteQuestionPipelineContext>> steps = List.of(
                new ValidateDeleteQuestionIdStep(),
                new FetchQuestionForDeleteStep(questionRepo),
                new CheckQuestionNotInUseStep(questionnaireRepo),
                new DeleteQuestionStep(questionRepo)
        );
        return new DeleteQuestionService(steps);
    }

    private static Question question(String id) {
        return Question.rehydrate(id, "Label " + id, ParameterizationStatus.ACTIVE, "SKU-1", audit());
    }

    private static AuditInfo audit() {
        return OrderQuestionnaireAuditFactory.createNew(
                Id.withId("99999999-9999-9999-9999-999999999999"),
                "REF", "Name", "name@acme.com",
                LocalDateTime.parse("2026-01-01T10:00:00")
        ).getOrElseThrow(error -> new IllegalStateException("Invalid audit test data: " + error));
    }
}

