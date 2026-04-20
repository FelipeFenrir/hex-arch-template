package com.acme.orderquestionnaire.application.question.service;

import com.acme.orderquestionnaire.application.question.dto.command.UpdateQuestionCommand;
import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionUpdatedView;
import com.acme.orderquestionnaire.application.question.service.UpdateQuestionService;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@DisplayName("UpdateQuestionService")
class UpdateQuestionServiceTest {

    private static final Id CREATED_BY_ID = Id.withId("33333333-3333-3333-3333-333333333333");
    private static final Id UPDATED_BY_ID = Id.withId("44444444-4444-4444-4444-444444444444");

    @Test
    @DisplayName("should update label and sales item while keeping status when command status is null")
    void shouldUpdateFieldsWithoutStatusChange() {
        QuestionCommandOutPort repository = mock(QuestionCommandOutPort.class);
        Question current = Question.rehydrate(
                "question_one",
                "Old label",
                ParameterizationStatus.DRAFT,
                "SKU-OLD",
                createdAudit()
        );

        when(repository.findQuestionById("question_one")).thenReturn(Optional.of(current));
        when(repository.update(any(Question.class))).thenAnswer(invocation -> Result.success(invocation.getArgument(0)));

        UpdateQuestionService service = new UpdateQuestionService(repository);
        UpdateQuestionCommand command = new UpdateQuestionCommand(
                "New label",
                "SKU-NEW",
                null,
                updatedByParam(),
                updatedAt()
        );

        Result<QuestionUpdatedView, List<DomainError>> result = service.execute("question_one", command);

        assertInstanceOf(Result.Success.class, result);
        QuestionUpdatedView view = result.getOrElseThrow(error ->
                new IllegalStateException("Expected success but got failure: " + error));
        assertEquals("New label", view.label());
        assertEquals("SKU-NEW", view.salesItemReferenceCode());
        assertEquals("DRAFT", view.status());
        assertEquals(UPDATED_BY_ID.stringfyId(), view.updatedBy().id());
        assertEquals(updatedAt(), view.updatedAt());
    }

    @Test
    @DisplayName("should allow status transition from ACTIVE to INACTIVE")
    void shouldAllowTransitionFromActiveToInactive() {
        QuestionCommandOutPort repository = mock(QuestionCommandOutPort.class);
        Question current = Question.rehydrate(
                "question_two",
                "Question",
                ParameterizationStatus.ACTIVE,
                "SKU-1",
                createdAudit()
        );

        when(repository.findQuestionById("question_two")).thenReturn(Optional.of(current));
        when(repository.update(any(Question.class))).thenAnswer(invocation -> Result.success(invocation.getArgument(0)));

        UpdateQuestionService service = new UpdateQuestionService(repository);
        UpdateQuestionCommand command = new UpdateQuestionCommand(
                "Question",
                "SKU-1",
                ParameterizationStatus.INACTIVE,
                updatedByParam(),
                updatedAt()
        );

        Result<QuestionUpdatedView, List<DomainError>> result = service.execute("question_two", command);

        assertInstanceOf(Result.Success.class, result);
        QuestionUpdatedView view = result.getOrElseThrow(error ->
                new IllegalStateException("Expected success but got failure: " + error));
        assertEquals("INACTIVE", view.status());
        assertEquals(UPDATED_BY_ID.stringfyId(), view.updatedBy().id());
    }

    @Test
    @DisplayName("should fail when status transition is not allowed")
    void shouldFailWhenStatusTransitionIsInvalid() {
        QuestionCommandOutPort repository = mock(QuestionCommandOutPort.class);
        Question current = Question.rehydrate(
                "question_three",
                "Question",
                ParameterizationStatus.DRAFT,
                "SKU-1",
                createdAudit()
        );

        when(repository.findQuestionById("question_three")).thenReturn(Optional.of(current));

        UpdateQuestionService service = new UpdateQuestionService(repository);
        UpdateQuestionCommand command = new UpdateQuestionCommand(
                "Question",
                "SKU-1",
                ParameterizationStatus.INACTIVE,
                updatedByParam(),
                updatedAt()
        );

        Result<QuestionUpdatedView, List<DomainError>> result = service.execute("question_three", command);

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("INVALID_STATUS_TRANSITION")));
        verify(repository, never()).update(any());
    }

    @Test
    @DisplayName("should fail when question is not found")
    void shouldFailWhenQuestionIsNotFound() {
        QuestionCommandOutPort repository = mock(QuestionCommandOutPort.class);
        when(repository.findQuestionById("missing")).thenReturn(Optional.empty());

        UpdateQuestionService service = new UpdateQuestionService(repository);
        UpdateQuestionCommand command = new UpdateQuestionCommand(
                "Question",
                "SKU-1",
                null,
                updatedByParam(),
                updatedAt()
        );

        Result<QuestionUpdatedView, List<DomainError>> result = service.execute("missing", command);

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("QUESTION_NOT_FOUND")));
    }

    private static AuditInfo createdAudit() {
        return OrderQuestionnaireAuditFactory.createNew(
                CREATED_BY_ID,
                "REF-CREATE",
                "Creator",
                "create@acme.com",
                LocalDateTime.parse("2026-01-10T08:00:00")
        ).getOrElseThrow(error -> new IllegalStateException("Invalid audit test data: " + error));
    }

    private static AuditUserParam updatedByParam() {
        return new AuditUserParam(UPDATED_BY_ID, "REF-UPD", "Updater", "update@acme.com");
    }

    private static LocalDateTime updatedAt() {
        return LocalDateTime.parse("2026-01-10T09:00:00");
    }
}

