package com.acme.orderquestionnaire.application.unit.questionnaire.service.step;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.CreateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.service.context.CreateQuestionnairePipelineContext;
import com.acme.orderquestionnaire.application.questionnaire.service.step.ValidateCommandStep;
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
@DisplayName("ValidateCommandStep (create questionnaire)")
class ValidateCommandStepTest {

    private final ValidateCommandStep step = new ValidateCommandStep();

    @Test
    @DisplayName("should fail when command is null")
    void shouldFailWhenCommandIsNull() {
        Result<Void, List<DomainError>> result = step.execute(new CreateQuestionnairePipelineContext(null));

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_COMMAND")));
    }

    @Test
    @DisplayName("should fail and accumulate required-field errors when id, channel, journey and description are blank")
    void shouldAccumulateAllRequiredFieldErrors() {
        var cmd = new CreateQuestionnaireCommand("", "", "", "", defaultUser(), defaultNow());
        Result<Void, List<DomainError>> result = step.execute(new CreateQuestionnairePipelineContext(cmd));

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("REQUIRED_FIELD") && e.message().contains("id")));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("REQUIRED_FIELD") && e.message().contains("channelDistributionId")));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("REQUIRED_FIELD") && e.message().contains("journeyDistributionId")));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("REQUIRED_FIELD") && e.message().contains("description")));
    }

    @Test
    @DisplayName("should succeed when all required fields are present")
    void shouldSucceedWhenAllFieldsAreValid() {
        var cmd = new CreateQuestionnaireCommand("q_001", "APP", "JOURNEY_01", "Description", defaultUser(), defaultNow());
        Result<Void, List<DomainError>> result = step.execute(new CreateQuestionnairePipelineContext(cmd));

        assertInstanceOf(Result.Success.class, result);
    }

    private static AuditUserParam defaultUser() {
        return new AuditUserParam(Id.withId("00000000-0000-0000-0000-000000000001"), "REF-1", "User", "u@acme.com");
    }

    private static LocalDateTime defaultNow() {
        return LocalDateTime.parse("2026-01-01T10:00:00");
    }
}

