package com.acme.orderquestionnaire.application.questionnaire.service;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.DeleteQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.DeleteQuestionnairesResultView;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.Id;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
@DisplayName("DeleteQuestionnaireService")
class DeleteQuestionnaireServiceTest {

    private QuestionnaireCommandOutPort questionnaireRepository;
    private DeleteQuestionnaireService service;

    @BeforeEach
    void setUp() {
        questionnaireRepository = mock(QuestionnaireCommandOutPort.class);
        service = new DeleteQuestionnaireService(questionnaireRepository);
        when(questionnaireRepository.deleteById(any())).thenReturn(Result.success(null));
    }

    @Test
    @DisplayName("should throw when questionnaireRepository is null")
    void shouldThrowWhenQuestionnaireRepositoryIsNull() {
        assertThrows(NullPointerException.class, () -> new DeleteQuestionnaireService(null));
    }

    @Test
    @DisplayName("execute(single): should delete questionnaire in DRAFT")
    void shouldDeleteWhenStatusIsDraft() {
        DeleteQuestionnaireCommand command = command("q_001", "APP", "J_1");
        when(questionnaireRepository.findQuestionnaireById(QuestionnaireId.of("q_001", "APP", "J_1")))
                .thenReturn(Optional.of(questionnaire(command, ParameterizationStatus.DRAFT)));

        Result<Void, List<DomainError>> result = service.execute(command);

        assertInstanceOf(Result.Success.class, result);
        verify(questionnaireRepository).deleteById(QuestionnaireId.of("q_001", "APP", "J_1"));
    }

    @Test
    @DisplayName("execute(single): should delete questionnaire in INACTIVE")
    void shouldDeleteWhenStatusIsInactive() {
        DeleteQuestionnaireCommand command = command("q_001", "APP", "J_1");
        when(questionnaireRepository.findQuestionnaireById(QuestionnaireId.of("q_001", "APP", "J_1")))
                .thenReturn(Optional.of(questionnaire(command, ParameterizationStatus.INACTIVE)));

        Result<Void, List<DomainError>> result = service.execute(command);

        assertInstanceOf(Result.Success.class, result);
        verify(questionnaireRepository).deleteById(QuestionnaireId.of("q_001", "APP", "J_1"));
    }

    @Test
    @DisplayName("execute(single): should fail when questionnaire status is ACTIVE")
    void shouldFailWhenStatusIsActive() {
        DeleteQuestionnaireCommand command = command("q_001", "APP", "J_1");
        when(questionnaireRepository.findQuestionnaireById(any()))
                .thenReturn(Optional.of(questionnaire(command, ParameterizationStatus.ACTIVE)));

        Result<Void, List<DomainError>> result = service.execute(command);

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("QUESTIONNAIRE_DELETE_NOT_ALLOWED")));
        verify(questionnaireRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("execute(single): QUESTIONNAIRE_DELETE_NOT_ALLOWED message should match snapshot")
    void shouldMatchDeleteNotAllowedMessageSnapshot() {
        DeleteQuestionnaireCommand command = command("q_001", "APP", "J_1");
        when(questionnaireRepository.findQuestionnaireById(any()))
                .thenReturn(Optional.of(questionnaire(command, ParameterizationStatus.ACTIVE)));

        Result<Void, List<DomainError>> result = service.execute(command);

        assertInstanceOf(Result.Failure.class, result);
        DomainError firstError = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success")).getFirst();
        assertEquals("QUESTIONNAIRE_DELETE_NOT_ALLOWED", firstError.code());
        assertEquals(
                "questionnaire 'q_001' cannot be deleted with status 'ACTIVE'; only DRAFT or INACTIVE are allowed",
                firstError.message()
        );
    }

    @Test
    @DisplayName("execute(single): should fail when questionnaire is not found")
    void shouldFailWhenQuestionnaireNotFound() {
        DeleteQuestionnaireCommand command = command("q_001", "APP", "J_1");
        when(questionnaireRepository.findQuestionnaireById(any())).thenReturn(Optional.empty());

        Result<Void, List<DomainError>> result = service.execute(command);

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("QUESTIONNAIRE_NOT_FOUND")));
        verify(questionnaireRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("execute(single): should fail when command has invalid composite id")
    void shouldFailWhenCommandIsInvalid() {
        DeleteQuestionnaireCommand invalid = command(" ", "APP", "");

        Result<Void, List<DomainError>> result = service.execute(invalid);

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("INVALID_ID")));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("INVALID_JOURNEY_DISTRIBUTION_ID")));
        verify(questionnaireRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("execute(batch): should process all commands and return only failures")
    void shouldProcessBatchAndReturnOnlyFailures() {
        DeleteQuestionnaireCommand draft = command("q_draft", "APP", "J_1");
        DeleteQuestionnaireCommand active = command("q_active", "APP", "J_1");
        DeleteQuestionnaireCommand missing = command("q_missing", "APP", "J_1");

        when(questionnaireRepository.findQuestionnaireById(QuestionnaireId.of("q_draft", "APP", "J_1")))
                .thenReturn(Optional.of(questionnaire(draft, ParameterizationStatus.DRAFT)));
        when(questionnaireRepository.findQuestionnaireById(QuestionnaireId.of("q_active", "APP", "J_1")))
                .thenReturn(Optional.of(questionnaire(active, ParameterizationStatus.ACTIVE)));
        when(questionnaireRepository.findQuestionnaireById(QuestionnaireId.of("q_missing", "APP", "J_1")))
                .thenReturn(Optional.empty());

        Result<DeleteQuestionnairesResultView, List<DomainError>> result = service.execute(List.of(draft, active, missing));

        assertInstanceOf(Result.Success.class, result);
        DeleteQuestionnairesResultView view = result.getOrElseThrow(errors ->
                new IllegalStateException("Expected success but got failure: " + errors));
        assertEquals(2, view.failures().size());
        assertTrue(view.failures().stream().anyMatch(failure ->
                failure.id().equals("q_active") && failure.code().equals("QUESTIONNAIRE_DELETE_NOT_ALLOWED")));
        assertTrue(view.failures().stream().anyMatch(failure ->
                failure.id().equals("q_missing") && failure.code().equals("QUESTIONNAIRE_NOT_FOUND")));

        verify(questionnaireRepository).deleteById(QuestionnaireId.of("q_draft", "APP", "J_1"));
        verify(questionnaireRepository, never()).deleteById(QuestionnaireId.of("q_active", "APP", "J_1"));
    }

    @Test
    @DisplayName("execute(batch): should keep full structured failure payload snapshot")
    void shouldKeepStructuredFailurePayloadSnapshot() {
        DeleteQuestionnaireCommand active = command("q_active", "APP", "J_1");
        when(questionnaireRepository.findQuestionnaireById(QuestionnaireId.of("q_active", "APP", "J_1")))
                .thenReturn(Optional.of(questionnaire(active, ParameterizationStatus.ACTIVE)));

        Result<DeleteQuestionnairesResultView, List<DomainError>> result = service.execute(List.of(active));

        assertInstanceOf(Result.Success.class, result);
        DeleteQuestionnairesResultView view = result.getOrElseThrow(errors ->
                new IllegalStateException("Expected success but got failure: " + errors));

        assertEquals(1, view.failures().size());
        var failure = view.failures().getFirst();
        assertEquals("q_active", failure.id());
        assertEquals("APP", failure.channelDistributionId());
        assertEquals("J_1", failure.journeyDistributionId());
        assertEquals("QUESTIONNAIRE_DELETE_NOT_ALLOWED", failure.code());
        assertEquals(
                "questionnaire 'q_active' cannot be deleted with status 'ACTIVE'; only DRAFT or INACTIVE are allowed",
                failure.message()
        );
    }

    @Test
    @DisplayName("execute(batch): should fail globally when commands list is null or empty")
    void shouldFailGloballyWhenCommandsAreInvalid() {
        Result<DeleteQuestionnairesResultView, List<DomainError>> nullResult = service.execute((List<DeleteQuestionnaireCommand>) null);
        Result<DeleteQuestionnairesResultView, List<DomainError>> emptyResult = service.execute(List.of());

        assertInstanceOf(Result.Failure.class, nullResult);
        assertInstanceOf(Result.Failure.class, emptyResult);
    }

    private static DeleteQuestionnaireCommand command(String id, String channel, String journey) {
        return new DeleteQuestionnaireCommand(id, channel, journey);
    }

    private static Questionnaire questionnaire(DeleteQuestionnaireCommand command, ParameterizationStatus status) {
        return Questionnaire.rehydrate(
                QuestionnaireId.of(command.id(), command.channelDistributionId(), command.journeyDistributionId()),
                "Questionnaire " + command.id(),
                status,
                List.of(),
                OrderQuestionnaireAuditFactory.createNew(
                                Id.withId("77777777-7777-7777-7777-777777777777"),
                                "REF",
                                "Creator",
                                "creator@acme.com",
                                LocalDateTime.parse("2026-01-01T10:00:00")
                        )
                        .getOrElseThrow(error -> new IllegalStateException("Invalid audit test data: " + error))
        );
    }
}

