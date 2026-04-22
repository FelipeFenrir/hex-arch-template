package com.acme.orderquestionnaire.domain.question;

import com.acme.orderquestionnaire.testutils.mocks.audit.AuditTestData;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("QuestionFactory")
class QuestionFactoryTest {

    @Test
    @DisplayName("When creating a new question, should keep draft status")
    void shouldCreateNewQuestion() {
        Question question = QuestionFactory
                .createNew("q_one", "Question 1", "SALE", AuditTestData.createdAudit())
                .flatMap(QuestionFactory.NewQuestionBuilder::build)
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        assertEquals("q_one", question.id());
        assertEquals("Question 1", question.label());
        assertEquals("SALE", question.salesItemReferenceCode());
        assertSame(ParameterizationStatus.DRAFT, question.status());
    }

    @Test
    @DisplayName("When rehydrating a question, should keep provided status")
    void shouldRehydrateQuestion() {
        Question question = QuestionFactory
                .rehydrate("q2", "Question 2", ParameterizationStatus.ACTIVE, "SALE", AuditTestData.createdAudit())
                .flatMap(QuestionFactory.RehydratedQuestionBuilder::build)
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        assertEquals("q2", question.id());
        assertEquals("Question 2", question.label());
        assertEquals("SALE", question.salesItemReferenceCode());
        assertSame(ParameterizationStatus.ACTIVE, question.status());
    }

    @Test
    @DisplayName("When creating with invalid id and label, should return accumulated errors")
    void shouldAccumulateErrorsForInvalidCreate() {
        Result<QuestionFactory.NewQuestionBuilder, List<DomainError>> result =
                QuestionFactory.createNew(" ", null, "SALE", AuditTestData.createdAudit());

        assertTrue(result.isFailure());
        List<DomainError> error = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertEquals(2, error.size());
    }

    @Test
    @DisplayName("When creating with camelCase id, should return INVALID_ID_FORMAT error")
    void shouldRejectCamelCaseId() {
        Result<QuestionFactory.NewQuestionBuilder, List<DomainError>> result =
                QuestionFactory.createNew("myQuestion", "Label", "SALE", AuditTestData.createdAudit());

        assertTrue(result.isFailure());
        List<DomainError> error = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertEquals(1, error.size());
        assertEquals("INVALID_ID_FORMAT", error.getFirst().code());
    }

    @Test
    @DisplayName("When creating with kebab-case id, should return INVALID_ID_FORMAT error")
    void shouldRejectKebabCaseId() {
        Result<QuestionFactory.NewQuestionBuilder, List<DomainError>> result =
                QuestionFactory.createNew("my-question", "Label", "SALE", AuditTestData.createdAudit());

        assertTrue(result.isFailure());
        List<DomainError> error = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertEquals("INVALID_ID_FORMAT", error.getFirst().code());
    }

    @Test
    @DisplayName("When creating with id containing uppercase, should return INVALID_ID_FORMAT error")
    void shouldRejectUpperCaseId() {
        Result<QuestionFactory.NewQuestionBuilder, List<DomainError>> result =
                QuestionFactory.createNew("My_Question", "Label", "SALE", AuditTestData.createdAudit());

        assertTrue(result.isFailure());
        List<DomainError> error = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertEquals("INVALID_ID_FORMAT", error.getFirst().code());
    }

    @Test
    @DisplayName("When creating with valid snake_case ids, should succeed")
    void shouldAcceptValidSnakeCaseIds() {
        assertFalse(QuestionFactory.createNew("my_question", "Label", "SALE", AuditTestData.createdAudit()).isFailure());
        assertFalse(QuestionFactory.createNew("question", "Label", "SALE", AuditTestData.createdAudit()).isFailure());
        assertFalse(QuestionFactory.createNew("q_one", "Label", "SALE", AuditTestData.createdAudit()).isFailure());
        assertFalse(QuestionFactory.createNew("my_question_2", "Label", "SALE", AuditTestData.createdAudit()).isFailure());
    }

    @Test
    @DisplayName("When creating with null auditInfo, should return REQUIRED_OBJECT error")
    void shouldRejectNullAuditInfoOnCreateNew() {
        Result<QuestionFactory.NewQuestionBuilder, List<DomainError>> result =
                QuestionFactory.createNew("q_one", "Label", "SALE", null);

        assertTrue(result.isFailure());
        List<DomainError> error = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertEquals(1, error.size());
        assertEquals("REQUIRED_OBJECT", error.getFirst().code());
    }

    @Test
    @DisplayName("When rehydrating with null auditInfo, should return REQUIRED_OBJECT error")
    void shouldRejectNullAuditInfoOnRehydrate() {
        Result<QuestionFactory.RehydratedQuestionBuilder, List<DomainError>> result =
                QuestionFactory.rehydrate("q_one", "Label", ParameterizationStatus.ACTIVE, "SALE", null);

        assertTrue(result.isFailure());
        List<DomainError> error = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertEquals(1, error.size());
        assertEquals("REQUIRED_OBJECT", error.getFirst().code());
    }
}
