package com.acme.orderquestionnaire.application.questionnaire.service;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.AnswerConfigParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.ConditionParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.ConfiguredQuestionParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateConfiguredQuestionParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireUpdatedView;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerConfigurationFactory;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.enumerator.ParameterizationStatus;
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
@DisplayName("UpdateQuestionnaireService")
class UpdateQuestionnaireServiceTest {

    private static final String QUESTIONNAIRE_ID = "q_001";
    private static final String CHANNEL = "APP";
    private static final String JOURNEY = "JOURNEY_01";

    private QuestionnaireCommandOutPort questionnaireRepository;
    private QuestionCommandOutPort questionRepository;
    private UpdateQuestionnaireService service;

    @BeforeEach
    void setUp() {
        questionnaireRepository = mock(QuestionnaireCommandOutPort.class);
        questionRepository = mock(QuestionCommandOutPort.class);
        service = new UpdateQuestionnaireService(questionnaireRepository, questionRepository);
    }

    @Test
    @DisplayName("should fail when command is null")
    void shouldFailWhenCommandIsNull() {
        Result<QuestionnaireUpdatedView, List<DomainError>> result = service.execute(null);
        assertFailureWithCode(result, "INVALID_COMMAND");
        verify(questionnaireRepository, never()).update(any());
    }

    @Test
    @DisplayName("should fail when questionnaire is not found")
    void shouldFailWhenQuestionnaireNotFound() {
        when(questionnaireRepository.findQuestionnaireById(any())).thenReturn(Optional.empty());

        Result<QuestionnaireUpdatedView, List<DomainError>> result = service.execute(baseCommand(null, null, null));

        assertFailureWithCode(result, "QUESTIONNAIRE_NOT_FOUND");
        verify(questionnaireRepository, never()).update(any());
    }

    @Test
    @DisplayName("should fail when active questionnaire receives structural change")
    void shouldFailWhenActiveQuestionnaireReceivesStructuralChange() {
        Questionnaire active = questionnaire(ParameterizationStatus.ACTIVE, List.of(existingConfigured()));
        when(questionnaireRepository.findQuestionnaireById(any())).thenReturn(Optional.of(active));

        UpdateConfiguredQuestionParam upsert = new UpdateConfiguredQuestionParam(
                "q_new",
                new ConfiguredQuestionParam(1, new AnswerConfigParam.Number(null, null, null, true, true, null), null));

        Result<QuestionnaireUpdatedView, List<DomainError>> result = service.execute(baseCommand(List.of(upsert), null, null));

        assertFailureWithCode(result, "QUESTIONNAIRE_UPDATE_NOT_ALLOWED");
        verify(questionnaireRepository, never()).update(any());
    }

    @Test
    @DisplayName("should deactivate active questionnaire when only status is changed")
    void shouldDeactivateActiveQuestionnaire() {
        Questionnaire active = questionnaire(ParameterizationStatus.ACTIVE, List.of(existingConfigured()));
        when(questionnaireRepository.findQuestionnaireById(any())).thenReturn(Optional.of(active));
        when(questionnaireRepository.update(any())).thenAnswer(inv -> Result.success(inv.getArgument(0)));

        UpdateQuestionnaireCommand command = new UpdateQuestionnaireCommand(
                QUESTIONNAIRE_ID,
                CHANNEL,
                JOURNEY,
                null,
                ParameterizationStatus.INACTIVE,
                List.of(),
                List.of(),
                updatedBy(),
                updatedAt());

        Result<QuestionnaireUpdatedView, List<DomainError>> result = service.execute(command);

        assertInstanceOf(Result.Success.class, result);
        QuestionnaireUpdatedView view = result.getOrElseThrow(error ->
                new IllegalStateException("Expected success but got failure: " + error));
        assertEquals("INACTIVE", view.status());
        assertEquals(updatedBy().id().stringfyId(), view.updatedBy().id());
        verify(questionnaireRepository).update(any());
    }

    @Test
    @DisplayName("should add configured question and activate questionnaire")
    void shouldAddConfiguredQuestionAndActivate() {
        Questionnaire draft = questionnaire(ParameterizationStatus.DRAFT, List.of());
        when(questionnaireRepository.findQuestionnaireById(any())).thenReturn(Optional.of(draft));
        when(questionRepository.findQuestionById("q_new")).thenReturn(Optional.of(question("q_new")));
        when(questionnaireRepository.update(any())).thenAnswer(inv -> Result.success(inv.getArgument(0)));

        UpdateConfiguredQuestionParam upsert = new UpdateConfiguredQuestionParam(
                "q_new",
                new ConfiguredQuestionParam(2, new AnswerConfigParam.Text("[0-9]+", "Digits"), null));

        UpdateQuestionnaireCommand command = new UpdateQuestionnaireCommand(
                QUESTIONNAIRE_ID,
                CHANNEL,
                JOURNEY,
                "Updated description",
                ParameterizationStatus.ACTIVE,
                List.of(upsert),
                List.of(),
                updatedBy(),
                updatedAt());

        Result<QuestionnaireUpdatedView, List<DomainError>> result = service.execute(command);

        assertInstanceOf(Result.Success.class, result);
        QuestionnaireUpdatedView view = result.getOrElseThrow(error ->
                new IllegalStateException("Expected success but got failure: " + error));
        assertEquals("ACTIVE", view.status());
        assertEquals("Updated description", view.description());
        assertEquals(1, view.configuredQuestions().size());
        assertEquals("q_new", view.configuredQuestions().getFirst().questionId());
        assertEquals("TEXT", view.configuredQuestions().getFirst().answerConfiguration().type());
        assertTrue(view.configuredQuestions().getFirst().dependsOnQuestionIds().isEmpty());
        verify(questionnaireRepository).update(any());
    }

    @Test
    @DisplayName("should add configured question with valid condition referencing existing questionnaire question")
    void shouldAddConfiguredQuestionWithValidCondition() {
        Questionnaire draft = questionnaire(
                ParameterizationStatus.DRAFT,
                List.of(existingConfigured())
        );
        when(questionnaireRepository.findQuestionnaireById(any())).thenReturn(Optional.of(draft));
        when(questionRepository.findQuestionById("q_income")).thenReturn(Optional.of(question("q_income")));
        when(questionnaireRepository.update(any())).thenAnswer(inv -> Result.success(inv.getArgument(0)));

        UpdateConfiguredQuestionParam upsert = new UpdateConfiguredQuestionParam(
                "q_income",
                new ConfiguredQuestionParam(
                        1,
                        new AnswerConfigParam.Number(null, null, null, true, true, null),
                        new ConditionParam.Equal("q_existing", "yes")
                )
        );

        Result<QuestionnaireUpdatedView, List<DomainError>> result = service.execute(
                baseCommand(List.of(upsert), List.of(), "Updated description")
        );

        assertInstanceOf(Result.Success.class, result);
        QuestionnaireUpdatedView view = result.getOrElseThrow(error ->
                new IllegalStateException("Expected success but got failure: " + error));
        assertEquals(2, view.configuredQuestions().size());
        var configuredQuestion = view.configuredQuestions().stream()
                .filter(item -> item.questionId().equals("q_income"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Configured question not found"));
        assertEquals("NUMBER", configuredQuestion.answerConfiguration().type());
        assertEquals("EQUAL", configuredQuestion.rootCondition().type());
        assertTrue(configuredQuestion.dependsOnQuestionIds().contains("q_existing"));
        verify(questionnaireRepository).update(any());
    }

    @Test
    @DisplayName("should fail when upsert param answerConfig is null")
    void shouldFailWhenUpsertAnswerConfigIsNull() {
        Questionnaire draft = questionnaire(ParameterizationStatus.DRAFT, List.of());
        when(questionnaireRepository.findQuestionnaireById(any())).thenReturn(Optional.of(draft));
        when(questionRepository.findQuestionById("q_new")).thenReturn(Optional.of(question("q_new")));

        UpdateConfiguredQuestionParam upsert = new UpdateConfiguredQuestionParam(
                "q_new",
                new ConfiguredQuestionParam(1, null, null)
        );

        Result<QuestionnaireUpdatedView, List<DomainError>> result = service.execute(
                baseCommand(List.of(upsert), List.of(), "Updated description")
        );

        assertFailureWithCode(result, "INVALID_COMMAND");
        verify(questionnaireRepository, never()).update(any());
    }

    @Test
    @DisplayName("should fail when activating without configured questions")
    void shouldFailWhenActivatingWithoutConfiguredQuestions() {
        Questionnaire draft = questionnaire(ParameterizationStatus.DRAFT, List.of());
        when(questionnaireRepository.findQuestionnaireById(any())).thenReturn(Optional.of(draft));

        UpdateQuestionnaireCommand command = new UpdateQuestionnaireCommand(
                QUESTIONNAIRE_ID,
                CHANNEL,
                JOURNEY,
                "Desc",
                ParameterizationStatus.ACTIVE,
                List.of(),
                List.of(),
                updatedBy(),
                updatedAt());

        Result<QuestionnaireUpdatedView, List<DomainError>> result = service.execute(command);

        assertFailureWithCode(result, "INVALID_CONFIGURED_QUESTION_FOR_ACTIVATION");
        verify(questionnaireRepository, never()).update(any());
    }

    @Test
    @DisplayName("should fail when condition references question absent from final questionnaire")
    void shouldFailWhenConditionReferenceIsUnknown() {
        Questionnaire draft = questionnaire(ParameterizationStatus.DRAFT, List.of());
        when(questionnaireRepository.findQuestionnaireById(any())).thenReturn(Optional.of(draft));
        when(questionRepository.findQuestionById("q_income")).thenReturn(Optional.of(question("q_income")));

        UpdateConfiguredQuestionParam upsert = new UpdateConfiguredQuestionParam(
                "q_income",
                new ConfiguredQuestionParam(
                        1,
                        new AnswerConfigParam.Number(null, null, null, true, true, null),
                        new ConditionParam.Equal("q_missing", "yes")));

        Result<QuestionnaireUpdatedView, List<DomainError>> result = service.execute(
                baseCommand(List.of(upsert), null, null));

        assertFailureWithCode(result, "CONDITION_QUESTION_NOT_FOUND");
        verify(questionnaireRepository, never()).update(any());
    }

    @Test
    @DisplayName("should fail when upsert question does not exist")
    void shouldFailWhenUpsertQuestionNotFound() {
        Questionnaire draft = questionnaire(ParameterizationStatus.DRAFT, List.of());
        when(questionnaireRepository.findQuestionnaireById(any())).thenReturn(Optional.of(draft));
        when(questionRepository.findQuestionById("q_missing")).thenReturn(Optional.empty());

        UpdateConfiguredQuestionParam upsert = new UpdateConfiguredQuestionParam(
                "q_missing",
                new ConfiguredQuestionParam(1, new AnswerConfigParam.Text(null, null), null));

        Result<QuestionnaireUpdatedView, List<DomainError>> result = service.execute(baseCommand(List.of(upsert), null, null));

        assertFailureWithCode(result, "QUESTION_NOT_FOUND");
        verify(questionnaireRepository, never()).update(any());
    }

    private static UpdateQuestionnaireCommand baseCommand(List<UpdateConfiguredQuestionParam> upserts,
                                                          List<String> removals,
                                                          String description) {
        return new UpdateQuestionnaireCommand(
                QUESTIONNAIRE_ID,
                CHANNEL,
                JOURNEY,
                description,
                null,
                upserts,
                removals,
                updatedBy(),
                updatedAt());
    }

    private static Questionnaire questionnaire(ParameterizationStatus status, List<ConfiguredQuestion> questions) {
        return Questionnaire.rehydrate(
                QuestionnaireId.of(QUESTIONNAIRE_ID, CHANNEL, JOURNEY),
                "Current description",
                status,
                questions,
                createdAudit());
    }

    private static ConfiguredQuestion existingConfigured() {
        return ConfiguredQuestion.createNew(question("q_existing"), AnswerConfigurationFactory.createTextStrategy(), 0);
    }

    private static Question question(String id) {
        return Question.rehydrate(id, "Label " + id, ParameterizationStatus.ACTIVE, "SKU-1", createdAudit());
    }

    private static AuditInfo createdAudit() {
        return OrderQuestionnaireAuditFactory.createNew(
                Id.withId("11111111-1111-1111-1111-111111111111"),
                "REF-CREATE",
                "Creator",
                "create@acme.com",
                LocalDateTime.parse("2026-01-01T10:00:00"))
                .getOrElseThrow(error -> new IllegalStateException("Invalid audit test data: " + error));
    }

    private static AuditUserParam updatedBy() {
        return new AuditUserParam(
                Id.withId("22222222-2222-2222-2222-222222222222"),
                "REF-UPD",
                "Updater",
                "update@acme.com");
    }

    private static LocalDateTime updatedAt() {
        return LocalDateTime.parse("2026-01-01T11:00:00");
    }

    private static void assertFailureWithCode(Result<QuestionnaireUpdatedView, List<DomainError>> result,
                                              String code) {
        assertInstanceOf(Result.Failure.class, result);
        Result.Failure<QuestionnaireUpdatedView, List<DomainError>> failure =
                (Result.Failure<QuestionnaireUpdatedView, List<DomainError>>) result;
        assertTrue(failure.error().stream().anyMatch(error -> error.code().equals(code)),
                "Expected error code '%s' but got %s".formatted(code, failure.error()));
    }
}

