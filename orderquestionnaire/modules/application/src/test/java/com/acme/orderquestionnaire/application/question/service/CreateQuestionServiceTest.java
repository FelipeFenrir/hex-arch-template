package com.acme.orderquestionnaire.application.question.service;

import com.acme.orderquestionnaire.application.question.dto.command.CreateQuestionCommand;
import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionCreatedView;
import com.acme.orderquestionnaire.application.question.service.CreateQuestionService;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@DisplayName("CreateQuestionService")
class CreateQuestionServiceTest {

    private static final Id DEFAULT_USER_ID = Id.withId("11111111-1111-1111-1111-111111111111");

    @Test
    @DisplayName("should return QuestionCreatedView when question is created successfully")
    void shouldReturnQuestionCreatedViewWhenQuestionIsCreated() {
        QuestionCommandOutPort repository = mock(QuestionCommandOutPort.class);
        when(repository.existsById("question_one")).thenReturn(false);
        when(repository.create(any(Question.class)))
                .thenAnswer(invocation -> Result.success(invocation.getArgument(0)));

        CreateQuestionService service = new CreateQuestionService(repository);
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
        QuestionCommandOutPort repository = mock(QuestionCommandOutPort.class);
        when(repository.existsById(any())).thenReturn(false);

        CreateQuestionService service = new CreateQuestionService(repository);

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
        QuestionCommandOutPort repository = mock(QuestionCommandOutPort.class);
        CreateQuestionService service = new CreateQuestionService(repository);

        Result<QuestionCreatedView, List<DomainError>> result =
                service.execute(new CreateQuestionCommand("", "Label", "SKU-1", defaultUser(), defaultNow()));

        assertInstanceOf(Result.Failure.class, result);
        verify(repository, never()).existsById(any());
        verify(repository, never()).create(any());
    }

    @Test
    @DisplayName("should fail when question already exists")
    void shouldFailWhenQuestionAlreadyExists() {
        QuestionCommandOutPort repository = mock(QuestionCommandOutPort.class);
        when(repository.existsById("question_three")).thenReturn(true);

        CreateQuestionService service = new CreateQuestionService(repository);

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
        QuestionCommandOutPort repository = mock(QuestionCommandOutPort.class);
        CreateQuestionService service = new CreateQuestionService(repository);

        Result<QuestionCreatedView, List<DomainError>> result = service.execute(null);

        assertInstanceOf(Result.Failure.class, result);
        verify(repository, never()).existsById(any());
        verify(repository, never()).create(any());
    }

    private static AuditUserParam defaultUser() {
        return new AuditUserParam(DEFAULT_USER_ID, "REF-1", "Test User", "test@acme.com");
    }

    private static LocalDateTime defaultNow() {
        return LocalDateTime.parse("2026-01-10T10:00:00");
    }
}
