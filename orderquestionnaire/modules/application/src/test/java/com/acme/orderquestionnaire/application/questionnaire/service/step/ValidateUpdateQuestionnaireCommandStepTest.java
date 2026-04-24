package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.AnswerConfigParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.ConfiguredQuestionParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateConfiguredQuestionParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.service.context.UpdateQuestionnairePipelineContext;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("ValidateUpdateQuestionnaireCommandStep")
class ValidateUpdateQuestionnaireCommandStepTest {

    private static final LocalDateTime FIXED_DATE = LocalDateTime.parse("2026-01-01T10:00:00");
    private final ValidateUpdateQuestionnaireCommandStep step = new ValidateUpdateQuestionnaireCommandStep();

    @Test
    @DisplayName("should fail with INVALID_COMMAND when command is null")
    void shouldFailWhenCommandIsNull() {
        Result<Void, List<DomainError>> result = step.execute(new UpdateQuestionnairePipelineContext(null));

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_COMMAND")));
    }

    @Test
    @DisplayName("should fail and accumulate REQUIRED_FIELD errors when id, channelDistributionId and journeyDistributionId are blank")
    void shouldAccumulateAllRequiredFieldErrors() {
        var cmd = new UpdateQuestionnaireCommand("", "", "", null, null, null, null, defaultUser(), FIXED_DATE);
        Result<Void, List<DomainError>> result = step.execute(new UpdateQuestionnairePipelineContext(cmd));

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("REQUIRED_FIELD") && e.message().contains("id")));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("REQUIRED_FIELD") && e.message().contains("channelDistributionId")));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("REQUIRED_FIELD") && e.message().contains("journeyDistributionId")));
    }

    @Test
    @DisplayName("should succeed with a valid command and no questions to upsert")
    void shouldSucceedWithValidMinimalCommand() {
        var cmd = new UpdateQuestionnaireCommand("q_001", "APP", "JOURNEY_01", null, null, null, null, defaultUser(), FIXED_DATE);
        Result<Void, List<DomainError>> result = step.execute(new UpdateQuestionnairePipelineContext(cmd));

        assertInstanceOf(Result.Success.class, result);
    }

    @Test
    @DisplayName("should fail with INVALID_COMMAND when questionsToUpsert contains null entry")
    void shouldFailWhenUpsertEntryIsNull() {
        List<UpdateConfiguredQuestionParam> upserts = new java.util.ArrayList<>();
        upserts.add(null);
        var cmd = new UpdateQuestionnaireCommand("q_001", "APP", "JOURNEY_01", null, null, upserts, null, defaultUser(), FIXED_DATE);
        Result<Void, List<DomainError>> result = step.execute(new UpdateQuestionnairePipelineContext(cmd));

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_COMMAND")));
    }

    @Test
    @DisplayName("should fail when upsert entry has blank questionId")
    void shouldFailWhenUpsertQuestionIdIsBlank() {
        var param = new ConfiguredQuestionParam(1, new AnswerConfigParam.Text(null, null), null);
        var upserts = List.of(new UpdateConfiguredQuestionParam("", param));
        var cmd = new UpdateQuestionnaireCommand("q_001", "APP", "JOURNEY_01", null, null, upserts, null, defaultUser(), FIXED_DATE);
        Result<Void, List<DomainError>> result = step.execute(new UpdateQuestionnairePipelineContext(cmd));

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("REQUIRED_FIELD")));
    }

    @Test
    @DisplayName("should fail with INVALID_COMMAND when upsert entry has null param")
    void shouldFailWhenUpsertParamIsNull() {
        var upserts = List.of(new UpdateConfiguredQuestionParam("q_valid", null));
        var cmd = new UpdateQuestionnaireCommand("q_001", "APP", "JOURNEY_01", null, null, upserts, null, defaultUser(), FIXED_DATE);
        Result<Void, List<DomainError>> result = step.execute(new UpdateQuestionnairePipelineContext(cmd));

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_COMMAND")));
    }

    private static AuditUserParam defaultUser() {
        return new AuditUserParam(Id.withId("00000000-0000-0000-0000-000000000001"), "REF-1", "User", "u@acme.com");
    }
}

