package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.CreateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.service.context.CreateQuestionnairePipelineContext;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("BuildAuditStep (create questionnaire)")
class BuildAuditStepTest {

    private final BuildAuditStep step = new BuildAuditStep();

    @Test
    @DisplayName("should fail when createdBy is null")
    void shouldFailWhenCreatedByIsNull() {
        var cmd = new CreateQuestionnaireCommand("q_001", "APP", "JOURNEY_01", "Desc", null, defaultNow());
        Result<Void, List<DomainError>> result = step.execute(new CreateQuestionnairePipelineContext(cmd));

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_USER_ID")));
    }

    @Test
    @DisplayName("should fail when createdAt is null")
    void shouldFailWhenCreatedAtIsNull() {
        var cmd = new CreateQuestionnaireCommand("q_001", "APP", "JOURNEY_01", "Desc", defaultUser(), null);
        Result<Void, List<DomainError>> result = step.execute(new CreateQuestionnairePipelineContext(cmd));

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_CREATED_AT")));
    }

    @Test
    @DisplayName("should succeed and store AuditInfo in context")
    void shouldSucceedAndStoreAuditInfoInContext() {
        var cmd = new CreateQuestionnaireCommand("q_001", "APP", "JOURNEY_01", "Desc", defaultUser(), defaultNow());
        CreateQuestionnairePipelineContext context = new CreateQuestionnairePipelineContext(cmd);

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Success.class, result);
        assertNotNull(context.auditInfo());
    }

    private static AuditUserParam defaultUser() {
        return new AuditUserParam(Id.withId("00000000-0000-0000-0000-000000000001"), "REF-1", "User", "u@acme.com");
    }

    private static LocalDateTime defaultNow() {
        return LocalDateTime.parse("2026-01-01T10:00:00");
    }
}

