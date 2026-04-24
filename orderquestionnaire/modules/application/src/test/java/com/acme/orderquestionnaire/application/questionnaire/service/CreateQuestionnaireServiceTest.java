package com.acme.orderquestionnaire.application.questionnaire.service;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.orderquestionnaire.application.channel.port.out.ChannelDistributionOutPort;
import com.acme.orderquestionnaire.application.journey.port.out.JourneyDistributionOutPort;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.CreateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireCreatedView;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.step.BuildAndPersistQuestionnaireStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.BuildAuditStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.CheckChannelDistributionStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.CheckJourneyDistributionStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.CheckNoDuplicateStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.ValidateCommandStep;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
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
@DisplayName("CreateQuestionnaireService")
public class CreateQuestionnaireServiceTest {

    private static final String Q_ID      = "q_001";
    private static final String CHANNEL   = "APP";
    private static final String JOURNEY   = "JOURNEY_01";
    private static final String DESC      = "Survey questionnaire";

    private QuestionnaireCommandOutPort  questionnaireRepository;
    private ChannelDistributionOutPort   channelPort;
    private JourneyDistributionOutPort   journeyPort;
    private CreateQuestionnaireService   service;

    @BeforeEach
    void setUp() {
        questionnaireRepository = mock(QuestionnaireCommandOutPort.class);
        channelPort  = mock(ChannelDistributionOutPort.class);
        journeyPort  = mock(JourneyDistributionOutPort.class);
        service = buildService(questionnaireRepository, channelPort, journeyPort);
    }

    // ── Constructor guards ────────────────────────────────────────────────────

    @Test
    @DisplayName("should throw when steps list is null")
    void shouldThrowWhenStepsListIsNull() {
        assertThrows(NullPointerException.class,
                () -> new CreateQuestionnaireService(null));
    }

    @Test
    @DisplayName("should throw when channelDistributionOutPort is null inside step")
    void shouldThrowWhenChannelPortIsNull() {
        assertThrows(NullPointerException.class,
                () -> new CheckChannelDistributionStep(null));
    }

    @Test
    @DisplayName("should throw when journeyDistributionOutPort is null inside step")
    void shouldThrowWhenJourneyPortIsNull() {
        assertThrows(NullPointerException.class,
                () -> new CheckJourneyDistributionStep(null));
    }

    @Test
    @DisplayName("should throw when questionnaireCommandOutPort is null inside CheckNoDuplicateStep")
    void shouldThrowWhenRepoIsNullInCheckNoDuplicate() {
        assertThrows(NullPointerException.class,
                () -> new CheckNoDuplicateStep(null));
    }

    @Test
    @DisplayName("should throw when questionnaireCommandOutPort is null inside BuildAndPersistStep")
    void shouldThrowWhenRepoIsNullInBuildAndPersist() {
        assertThrows(NullPointerException.class,
                () -> new BuildAndPersistQuestionnaireStep(null, RollbackStyle.FRAMEWORK_TRANSACTION));
    }

    // ── Validation failures ───────────────────────────────────────────────────

    @Test
    @DisplayName("should fail when command is null")
    void shouldFailWhenCommandIsNull() {
        assertFailureWithCode(service.execute(null), "INVALID_COMMAND");
        verify(questionnaireRepository, never()).create(any());
    }

    @Test
    @DisplayName("should fail when id is blank")
    void shouldFailWhenIdIsBlank() {
        assertFailureWithCodeAndMessage(service.execute(command("", CHANNEL, JOURNEY, DESC)),
                "REQUIRED_FIELD", "id");
    }

    @Test
    @DisplayName("should fail when channelDistributionId is blank")
    void shouldFailWhenChannelIsBlank() {
        assertFailureWithCodeAndMessage(service.execute(command(Q_ID, "", JOURNEY, DESC)),
                "REQUIRED_FIELD", "channelDistributionId");
    }

    @Test
    @DisplayName("should fail when journeyDistributionId is blank")
    void shouldFailWhenJourneyIsBlank() {
        assertFailureWithCodeAndMessage(service.execute(command(Q_ID, CHANNEL, "", DESC)),
                "REQUIRED_FIELD", "journeyDistributionId");
    }

    @Test
    @DisplayName("should fail when description is blank")
    void shouldFailWhenDescriptionIsBlank() {
        assertFailureWithCodeAndMessage(service.execute(command(Q_ID, CHANNEL, JOURNEY, "")),
                "REQUIRED_FIELD", "description");
    }

    @Test
    @DisplayName("should fail when createdBy is null with user id validation")
    void shouldFailWhenCreatedByIsNull() {
        var cmd = new CreateQuestionnaireCommand(Q_ID, CHANNEL, JOURNEY, DESC, null, defaultNow());
        assertFailureWithCode(service.execute(cmd), "INVALID_USER_ID");
    }

    @Test
    @DisplayName("should fail when createdAt is null")
    void shouldFailWhenCreatedAtIsNull() {
        var cmd = new CreateQuestionnaireCommand(Q_ID, CHANNEL, JOURNEY, DESC, defaultUser(), null);
        assertFailureWithCode(service.execute(cmd), "INVALID_CREATED_AT");
    }

    @Test
    @DisplayName("should fail when createdBy id is null")
    void shouldFailWhenCreatedByIdIsNull() {
        var cmd = new CreateQuestionnaireCommand(Q_ID, CHANNEL, JOURNEY, DESC,
                new AuditUserParam(null, "REF", "Name", "email@acme.com"), defaultNow());
        assertFailureWithCode(service.execute(cmd), "INVALID_USER_ID");
    }

    @Test
    @DisplayName("should fail when createdBy name is blank")
    void shouldFailWhenCreatedByNameIsBlank() {
        var cmd = new CreateQuestionnaireCommand(Q_ID, CHANNEL, JOURNEY, DESC,
                new AuditUserParam(Id.withId("00000000-0000-0000-0000-000000000001"), "REF", "", "email@acme.com"),
                defaultNow());
        assertFailureWithCode(service.execute(cmd), "INVALID_USER_NAME");
    }

    // ── Business rule failures ────────────────────────────────────────────────

    @Test
    @DisplayName("should fail when channel distribution is not found")
    void shouldFailWhenChannelNotFound() {
        when(channelPort.existsById(CHANNEL)).thenReturn(false);
        assertFailureWithCode(service.execute(validCommand()), "CHANNEL_DISTRIBUTION_NOT_FOUND");
        verify(questionnaireRepository, never()).create(any());
    }

    @Test
    @DisplayName("should fail when journey distribution is not found")
    void shouldFailWhenJourneyNotFound() {
        when(channelPort.existsById(CHANNEL)).thenReturn(true);
        when(journeyPort.existsById(JOURNEY)).thenReturn(false);
        assertFailureWithCode(service.execute(validCommand()), "JOURNEY_DISTRIBUTION_NOT_FOUND");
        verify(questionnaireRepository, never()).create(any());
    }

    @Test
    @DisplayName("should fail when questionnaire already exists")
    void shouldFailWhenAlreadyExists() {
        when(channelPort.existsById(CHANNEL)).thenReturn(true);
        when(journeyPort.existsById(JOURNEY)).thenReturn(true);
        when(questionnaireRepository.existsById(any())).thenReturn(true);
        assertFailureWithCode(service.execute(validCommand()), "QUESTIONNAIRE_ALREADY_EXISTS");
        verify(questionnaireRepository, never()).create(any());
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("should return QuestionnaireCreatedView when questionnaire is created successfully")
    void shouldCreateQuestionnaire() {
        when(channelPort.existsById(CHANNEL)).thenReturn(true);
        when(journeyPort.existsById(JOURNEY)).thenReturn(true);
        when(questionnaireRepository.existsById(any())).thenReturn(false);
        when(questionnaireRepository.create(any(Questionnaire.class)))
                .thenAnswer(inv -> Result.success(inv.getArgument(0)));

        Result<QuestionnaireCreatedView, List<DomainError>> result = service.execute(validCommand());

        assertInstanceOf(Result.Success.class, result);
        QuestionnaireCreatedView view = result.getOrElseThrow(error ->
                new IllegalStateException("Expected success but got failure: " + error));
        assertEquals(Q_ID, view.id());
        assertEquals(CHANNEL, view.channelDistributionId());
        assertEquals(JOURNEY, view.journeyDistributionId());
        assertEquals(DESC, view.description());
        assertEquals("DRAFT", view.status());
        verify(questionnaireRepository).create(any());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds a fully wired {@link CreateQuestionnaireService} with the default step set.
     * Use in test setup and in {@link com.acme.orderquestionnaire.application.FunctionalTest}.
     */
    public static CreateQuestionnaireService buildService(
            QuestionnaireCommandOutPort repo,
            ChannelDistributionOutPort channelPort,
            JourneyDistributionOutPort journeyPort) {
        List<Step<com.acme.orderquestionnaire.application.questionnaire.service.context.CreateQuestionnairePipelineContext>> steps = List.of(
                new ValidateCommandStep(),
                new BuildAuditStep(),
                new CheckChannelDistributionStep(channelPort),
                new CheckJourneyDistributionStep(journeyPort),
                new CheckNoDuplicateStep(repo),
                new BuildAndPersistQuestionnaireStep(repo, RollbackStyle.FRAMEWORK_TRANSACTION)
        );
        return new CreateQuestionnaireService(steps);
    }

    private CreateQuestionnaireCommand validCommand() {
        return new CreateQuestionnaireCommand(Q_ID, CHANNEL, JOURNEY, DESC, defaultUser(), defaultNow());
    }

    private static CreateQuestionnaireCommand command(String id, String channel, String journey, String desc) {
        return new CreateQuestionnaireCommand(id, channel, journey, desc, defaultUser(), defaultNow());
    }

    private static AuditUserParam defaultUser() {
        return new AuditUserParam(
                Id.withId("00000000-0000-0000-0000-000000000001"), "REF-1", "Test User", "test@acme.com");
    }

    private static LocalDateTime defaultNow() {
        return LocalDateTime.parse("2026-01-01T10:00:00");
    }

    private static void assertFailureWithCode(Result<?, List<DomainError>> result, String code) {
        assertInstanceOf(Result.Failure.class, result);
        var failure = (Result.Failure<?, List<DomainError>>) result;
        assertTrue(failure.error().stream().anyMatch(e -> e.code().equals(code)),
                "Expected error '%s' but got: %s".formatted(code, failure.error()));
    }

    private static void assertFailureWithCodeAndMessage(Result<?, List<DomainError>> result,
                                                        String code,
                                                        String messageFragment) {
        assertInstanceOf(Result.Failure.class, result);
        var failure = (Result.Failure<?, List<DomainError>>) result;
        assertTrue(failure.error().stream()
                        .anyMatch(e -> e.code().equals(code) && e.message().contains(messageFragment)),
                "Expected error '%s' containing '%s' but got: %s"
                        .formatted(code, messageFragment, failure.error()));
    }
}

