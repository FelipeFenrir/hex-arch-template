package com.acme.orderquestionnaire.application.question.service;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.orderquestionnaire.application.question.dto.command.CreateQuestionCommand;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionCreatedView;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.question.service.context.CreateQuestionPipelineContext;
import com.acme.orderquestionnaire.application.question.service.step.BuildAndPersistQuestionStep;
import com.acme.orderquestionnaire.application.question.service.step.BuildCreateQuestionAuditStep;
import com.acme.orderquestionnaire.application.question.service.step.CheckNoDuplicateStep;
import com.acme.orderquestionnaire.application.question.service.step.ValidateCreateQuestionCommandStep;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.shared.pattern.pipeline.RollbackStyle;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.Id;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@DisplayName("CreateQuestionService")
public class CreateQuestionServiceTest {

    private static final Id DEFAULT_USER_ID = Id.withId("11111111-1111-1111-1111-111111111111");

    private QuestionCommandOutPort repository;
    private CreateQuestionService service;

    @BeforeEach
    void setUp() {
        repository = mock(QuestionCommandOutPort.class);
        service = buildService(repository);
    }

    @Test
    @DisplayName("should throw when steps list is null")
    void shouldThrowWhenStepsListIsNull() {
        assertThrows(NullPointerException.class, () -> new CreateQuestionService(null));
    }

    @Test
    @DisplayName("should throw when questionCommandOutPort is null inside CheckNoDuplicateStep")
    void shouldThrowWhenRepositoryIsNullInCheckNoDuplicateStep() {
        assertThrows(NullPointerException.class, () -> new CheckNoDuplicateStep(null));
    }

    @Test
    @DisplayName("should throw when questionCommandOutPort is null inside BuildAndPersistQuestionStep")
    void shouldThrowWhenRepositoryIsNullInBuildAndPersistStep() {
        assertThrows(NullPointerException.class,
                () -> new BuildAndPersistQuestionStep(null, RollbackStyle.FRAMEWORK_TRANSACTION));
    }

    @Test
    @DisplayName("should return QuestionCreatedView when question is created successfully")
    void shouldReturnQuestionCreatedViewWhenQuestionIsCreated() {
        when(repository.existsById("question_one")).thenReturn(false);
        when(repository.create(any(Question.class)))
                .thenAnswer(invocation -> Result.success(invocation.getArgument(0)));

        CreateQuestionCommand command = new CreateQuestionCommand(
                "question_one", "Age", "SKU-1", defaultUser(), defaultNow());

        Result<QuestionCreatedView, List<DomainError>> result = service.execute(command);

        assertInstanceOf(Result.Success.class, result);
        QuestionCreatedView view = result.getOrElseThrow(error ->
                new IllegalStateException("Expected success but got failure: " + error));
        assertEquals("question_one", view.id());
        assertEquals("Age", view.label());
        assertEquals("DRAFT", view.status());
        assertEquals("SKU-1", view.salesItemReferenceCode());
        assertEquals(DEFAULT_USER_ID.stringfyId(), view.createdBy().id());
        assertEquals(defaultNow(), view.createdAt());
        verify(repository).existsById("question_one");
        verify(repository).create(any(Question.class));
    }

    @Test
    @DisplayName("should fail when id is not in snake_case format")
    void shouldFailWhenIdIsNotSnakeCase() {
        when(repository.existsById(any())).thenReturn(false);

        List<String> invalidIds = List.of("myQuestion", "my-question", "MyQuestion", "question1", "QUESTION");
        for (String invalidId : invalidIds) {
            Result<QuestionCreatedView, List<DomainError>> result =
                    service.execute(new CreateQuestionCommand(invalidId, "Label", "SKU-1", defaultUser(), defaultNow()));

            assertInstanceOf(Result.Failure.class, result,
                    "Expected failure for id: " + invalidId);
            List<DomainError> errors = result.errorOrElseThrow(() ->
                    new IllegalStateException("Expected failure for id: " + invalidId));
            assertTrue(errors.stream()
                    .anyMatch(e -> e.code().equals("INVALID_ID_FORMAT")),
                    "Expected INVALID_ID_FORMAT for id: " + invalidId);
        }
        verify(repository, never()).create(any());
    }

    @Test
    @DisplayName("should fail when id is blank")
    void shouldFailWhenIdIsBlank() {
        Result<QuestionCreatedView, List<DomainError>> result =
                service.execute(new CreateQuestionCommand("", "Label", "SKU-1", defaultUser(), defaultNow()));

        assertInstanceOf(Result.Failure.class, result);
        verify(repository, never()).existsById(any());
        verify(repository, never()).create(any());
    }

    @Test
    @DisplayName("should fail when question already exists")
    void shouldFailWhenQuestionAlreadyExists() {
        when(repository.existsById("question_three")).thenReturn(true);

        Result<QuestionCreatedView, List<DomainError>> result = service.execute(
                new CreateQuestionCommand("question_three", "Name", "SKU-3", defaultUser(), defaultNow())
        );

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure"));
        assertEquals(1, errors.size());
        assertEquals("QUESTION_ALREADY_EXISTS", errors.getFirst().code());
        assertTrue(errors.getFirst().message().contains("question_three"));
        verify(repository).existsById("question_three");
        verify(repository, never()).create(any());
    }

    @Test
    @DisplayName("should fail when command is null")
    void shouldFailWhenCommandIsNull() {
        Result<QuestionCreatedView, List<DomainError>> result = service.execute(null);

        assertInstanceOf(Result.Failure.class, result);
        verify(repository, never()).existsById(any());
        verify(repository, never()).create(any());
    }

    @Test
    @DisplayName("should fail when createdBy is null")
    void shouldFailWhenCreatedByIsNull() {
        Result<QuestionCreatedView, List<DomainError>> result = service.execute(
                new CreateQuestionCommand("question_one", "Label", "SKU-1", null, defaultNow())
        );

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("INVALID_USER_ID")));
        verify(repository).existsById("question_one");
        verify(repository, never()).create(any());
    }

    @Test
    @DisplayName("should fail when createdAt is null")
    void shouldFailWhenCreatedAtIsNull() {
        Result<QuestionCreatedView, List<DomainError>> result = service.execute(
                new CreateQuestionCommand("question_one", "Label", "SKU-1", defaultUser(), null)
        );

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("INVALID_CREATED_AT")));
        verify(repository).existsById("question_one");
        verify(repository, never()).create(any());
    }

    /**
     * Builds a fully wired {@link CreateQuestionService} with the default step set.
     * Use in test setup, functional tests, and BDD step definitions.
     */
    public static CreateQuestionService buildService(QuestionCommandOutPort repository) {
        List<Step<CreateQuestionPipelineContext>> steps = List.of(
                new ValidateCreateQuestionCommandStep(),
                new CheckNoDuplicateStep(repository),
                new BuildCreateQuestionAuditStep(),
                new BuildAndPersistQuestionStep(repository, RollbackStyle.FRAMEWORK_TRANSACTION)
        );
        return new CreateQuestionService(steps);
    }

    private static AuditUserParam defaultUser() {
        return new AuditUserParam(DEFAULT_USER_ID, "REF-1", "Test User", "test@acme.com");
    }

    private static LocalDateTime defaultNow() {
        return LocalDateTime.parse("2026-01-10T10:00:00");
    }
}
