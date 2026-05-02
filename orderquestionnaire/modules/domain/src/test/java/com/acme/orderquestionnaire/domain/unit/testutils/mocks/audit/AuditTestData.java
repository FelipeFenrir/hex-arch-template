package com.acme.orderquestionnaire.domain.unit.testutils.mocks.audit;

import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.shared.stereotypes.test.MockClass;
import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.Id;

import java.time.LocalDateTime;

@MockClass
public final class AuditTestData {

    private static final Id DEFAULT_USER_ID = Id.withId("019d7068-8b6b-7ead-92f5-4f1fcb519921");
    private static final String DEFAULT_REFERENCE_CODE = "REF-TEST";
    private static final String DEFAULT_NAME = "Test User";
    private static final String DEFAULT_EMAIL = "test@acme.com";
    private static final LocalDateTime DEFAULT_CREATED_AT = LocalDateTime.parse("2026-01-01T10:00:00");

    private AuditTestData() {
        throw new IllegalStateException("Utility class");
    }

    public static AuditInfo createdAudit() {
        return OrderQuestionnaireAuditFactory.createNew(
                DEFAULT_USER_ID,
                DEFAULT_REFERENCE_CODE,
                DEFAULT_NAME,
                DEFAULT_EMAIL,
                DEFAULT_CREATED_AT
        ).getOrElseThrow(error -> new IllegalStateException("Invalid created audit test data: " + error));
    }

    public static AuditInfo updatedAudit() {
        return createdAudit().withUpdate(
                OrderQuestionnaireAuditFactory.user(Id.withId("019d7068-d073-7c2b-ac33-60e5e2f6b159"),
                                "REF-UPD", "Updated User", "updated@acme.com")
                        .getOrElseThrow(error -> new IllegalStateException("Invalid updated user test data: " + error)),
                LocalDateTime.parse("2026-01-02T10:00:00")
        );
    }
}

