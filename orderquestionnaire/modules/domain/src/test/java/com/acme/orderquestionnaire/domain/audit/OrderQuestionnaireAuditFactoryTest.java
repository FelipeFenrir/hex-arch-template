package com.acme.orderquestionnaire.domain.audit;

import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.AuditUser;
import com.acme.shared.vo.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("OrderQuestionnaireAuditFactory")
class OrderQuestionnaireAuditFactoryTest {

    @Test
    @DisplayName("should fail createNew when createdAt is null")
    void shouldFailWhenCreatedAtIsNull() {
        Result<AuditInfo, List<DomainError>> result = OrderQuestionnaireAuditFactory.createNew(
                Id.withId("11111111-1111-1111-1111-111111111111"),
                "REF",
                "User",
                "user@acme.com",
                null
        );

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("INVALID_CREATED_AT")));
    }

    @Test
    @DisplayName("should accumulate user errors when user data is missing")
    void shouldAccumulateUserErrorsWhenDataMissing() {
        Result<AuditInfo, List<DomainError>> result = OrderQuestionnaireAuditFactory.createNew(
                null,
                "REF",
                " ",
                "user@acme.com",
                LocalDateTime.parse("2026-01-01T10:00:00")
        );

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("INVALID_USER_ID")));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("INVALID_USER_NAME")));
    }

    @Test
    @DisplayName("should create audit info successfully")
    void shouldCreateAuditInfoSuccessfully() {
        Result<AuditInfo, List<DomainError>> result = OrderQuestionnaireAuditFactory.createNew(
                Id.withId("11111111-1111-1111-1111-111111111111"),
                "REF",
                "User",
                "user@acme.com",
                LocalDateTime.parse("2026-01-01T10:00:00")
        );

        assertInstanceOf(Result.Success.class, result);
        AuditInfo auditInfo = result.getOrElseThrow(error -> new IllegalStateException("Expected success"));
        assertEquals("11111111-1111-1111-1111-111111111111", auditInfo.createdBy().id().stringfyId());
    }

    @Test
    @DisplayName("should fail update user when updatedAt is null")
    void shouldFailUpdateUserWhenUpdatedAtIsNull() {
        Result<AuditUser, List<DomainError>> result = OrderQuestionnaireAuditFactory.updateUserForQuestion(
                Id.withId("11111111-1111-1111-1111-111111111111"),
                "REF",
                "User",
                "user@acme.com",
                null
        );

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals("INVALID_UPDATED_AT")));
    }
}

