package com.acme.orderquestionnaire.domain.question;

import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.orderquestionnaire.testutils.mocks.audit.AuditTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("Question")
class QuestionTest {

    @Test
    @DisplayName("When creating new question then status should be draft")
    void shouldCreateDraftQuestion() {
        Question question = Question.createNew("q_one", "Question 1", "SALE", AuditTestData.createdAudit());

        assertEquals("q_one", question.id());
        assertEquals("Question 1", question.label());
        assertEquals("SALE", question.salesItemReferenceCode());
        assertSame(ParameterizationStatus.DRAFT, question.status());
        assertFalse(question.isActive());
    }

    @Test
    @DisplayName("When rehydrating active question then isActive should be true")
    void shouldRehydrateActiveQuestion() {
        Question question = Question.rehydrate("q_two", "Question 2", ParameterizationStatus.ACTIVE,
                "SALE", AuditTestData.createdAudit());

        assertTrue(question.isActive());
        assertSame(ParameterizationStatus.ACTIVE, question.status());
    }
}

