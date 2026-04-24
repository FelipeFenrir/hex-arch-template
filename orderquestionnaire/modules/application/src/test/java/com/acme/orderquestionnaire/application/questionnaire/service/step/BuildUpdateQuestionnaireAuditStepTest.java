package com.acme.orderquestionnaire.application.questionnaire.service.step;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("BuildUpdateQuestionnaireAuditStep")
class BuildUpdateQuestionnaireAuditStepTest {

    private static final LocalDateTime FIXED_DATE = LocalDateTime.parse("2026-01-02T10:00:00");
    private final BuildUpdateQuestionnaireAuditStep step = new BuildUpdateQuestionnaireAuditStep();

    @Test
    @DisplayName("should fail when updatedBy is null")
    void shouldFailWhenUpdatedByIsNull() {
        var cmd = updateCommand(null, FIXED_DATE);
        Result<Void, List<DomainError>> result = step.execute(new UpdateQuestionnairePipelineContext(cmd));

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_USER_ID")));
    }

    @Test
    @DisplayName("should fail when updatedAt is null")
    void shouldFailWhenUpdatedAtIsNull() {
        var cmd = updateCommand(defaultUser(), null);
        Result<Void, List<DomainError>> result = step.execute(new UpdateQuestionnairePipelineContext(cmd));

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals("INVALID_UPDATED_AT")));
    }

    @Test
    @DisplayName("should succeed and store updatedActor in context")
    void shouldSucceedAndStoreActorInContext() {
        var cmd = updateCommand(defaultUser(), FIXED_DATE);
        UpdateQuestionnairePipelineContext context = new UpdateQuestionnairePipelineContext(cmd);

        Result<Void, List<DomainError>> result = step.execute(context);

        assertInstanceOf(Result.Success.class, result);
        assertNotNull(context.updatedActor());
    }

    private static UpdateQuestionnaireCommand updateCommand(AuditUserParam updatedBy, LocalDateTime updatedAt) {
        return new UpdateQuestionnaireCommand("q_001", "APP", "JOURNEY_01", "Desc", null, null, null, updatedBy, updatedAt);
    }

    private static AuditUserParam defaultUser() {
        return new AuditUserParam(Id.withId("00000000-0000-0000-0000-000000000002"), "REF-2", "Updater", "upd@acme.com");
    }
}

