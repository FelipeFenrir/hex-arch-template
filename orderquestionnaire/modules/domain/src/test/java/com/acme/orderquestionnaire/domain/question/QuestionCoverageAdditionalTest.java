package com.acme.orderquestionnaire.domain.question;

import com.acme.orderquestionnaire.domain.question.errors.QuestionDomainErrors;
import com.acme.orderquestionnaire.domain.questionnaire.answer.AnswerOptionItem;
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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("Question additional coverage")
class QuestionCoverageAdditionalTest {

    @Test
    @DisplayName("Question should expose status and activity for new and rehydrated instances")
    void shouldExposeQuestionStateAndActivity() {
        Question draftQuestion = Question.createNew("new-id", "New question", "SALE", AuditTestData.createdAudit());
        Question activeQuestion = Question.rehydrate("rehydrated-id", "Existing question",
                ParameterizationStatus.ACTIVE, "SALE", AuditTestData.createdAudit());

        assertEquals("new-id", draftQuestion.id());
        assertFalse(draftQuestion.isActive());
        assertSame(ParameterizationStatus.DRAFT, draftQuestion.status());

        assertEquals("rehydrated-id", activeQuestion.id());
        assertTrue(activeQuestion.isActive());
        assertSame(ParameterizationStatus.ACTIVE, activeQuestion.status());
    }

    @Test
    @DisplayName("QuestionFactory should fail when sales item reference code is invalid")
    void shouldFailWhenSalesItemReferenceCodeIsInvalid() {
        Result<QuestionFactory.NewQuestionBuilder, List<DomainError>> invalidCreate =
                QuestionFactory.createNew("q_one", "Question 1", " ", AuditTestData.createdAudit());

        Result<QuestionFactory.RehydratedQuestionBuilder, List<DomainError>> invalidRehydrate =
                QuestionFactory.rehydrate("q_two", "Question 2", ParameterizationStatus.ACTIVE, null, AuditTestData.createdAudit());

        assertInstanceOf(Result.Failure.class, invalidCreate);
        List<DomainError> newErrors = invalidCreate.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure"));
        assertEquals(List.of(QuestionDomainErrors.requiredField("salesItemReferenceCode")), newErrors);

        assertInstanceOf(Result.Failure.class, invalidRehydrate);
        List<DomainError> rehydratedErrors = invalidRehydrate.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure"));
        assertEquals(List.of(QuestionDomainErrors.requiredField("salesItemReferenceCode")), rehydratedErrors);
    }

    @Test
    @DisplayName("QuestionFactory should reject rehydration without status")
    void shouldRejectRehydrateWithoutStatus() {
        Result<QuestionFactory.RehydratedQuestionBuilder, List<DomainError>> result =
                QuestionFactory.rehydrate("q_one", "Question 1", null, "SALE", AuditTestData.createdAudit());

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure"));
        assertEquals(List.of(QuestionDomainErrors.requiredObject("status")), errors);
    }

    @Test
    @DisplayName("QuestionFactory should also reject null id and label values")
    void shouldRejectNullIdAndLabelValues() {
        Result<QuestionFactory.NewQuestionBuilder, List<DomainError>> invalidCreate =
                QuestionFactory.createNew(null, "Question 1", "SALE", AuditTestData.createdAudit());
        Result<QuestionFactory.RehydratedQuestionBuilder, List<DomainError>> invalidRehydrate =
                QuestionFactory.rehydrate("q_one", null, ParameterizationStatus.ACTIVE, "SALE", AuditTestData.createdAudit());
        Result<QuestionFactory.RehydratedQuestionBuilder, List<DomainError>> nullIdRehydrate =
                QuestionFactory.rehydrate(null, "Question", ParameterizationStatus.ACTIVE, "SALE", AuditTestData.createdAudit());
        Result<QuestionFactory.RehydratedQuestionBuilder, List<DomainError>> blankIdRehydrate =
                QuestionFactory.rehydrate(" ", "Question", ParameterizationStatus.ACTIVE, "SALE", AuditTestData.createdAudit());
        Result<QuestionFactory.RehydratedQuestionBuilder, List<DomainError>> blankLabelRehydrate =
                QuestionFactory.rehydrate("q_one", " ", ParameterizationStatus.ACTIVE, "SALE", AuditTestData.createdAudit());

        assertInstanceOf(Result.Failure.class, invalidCreate);
        assertEquals(List.of(QuestionDomainErrors.requiredField("id")),
                invalidCreate.errorOrElseThrow(() -> new IllegalStateException("Expected failure")));

        assertInstanceOf(Result.Failure.class, invalidRehydrate);
        assertEquals(List.of(QuestionDomainErrors.requiredField("label")),
                invalidRehydrate.errorOrElseThrow(() -> new IllegalStateException("Expected failure")));

        assertInstanceOf(Result.Failure.class, blankIdRehydrate);
        assertEquals(List.of(QuestionDomainErrors.requiredField("id")),
                blankIdRehydrate.errorOrElseThrow(() -> new IllegalStateException("Expected failure")));

        assertInstanceOf(Result.Failure.class, nullIdRehydrate);
        assertEquals(List.of(QuestionDomainErrors.requiredField("id")),
                nullIdRehydrate.errorOrElseThrow(() -> new IllegalStateException("Expected failure")));

        assertInstanceOf(Result.Failure.class, blankLabelRehydrate);
        assertEquals(List.of(QuestionDomainErrors.requiredField("label")),
                blankLabelRehydrate.errorOrElseThrow(() -> new IllegalStateException("Expected failure")));
    }

    @Test
    @DisplayName("AnswerOptionItem should expose value and label on creation and rehydration")
    void shouldSupportAnswerOptionItemRehydrationAndState() {
        AnswerOptionItem created = AnswerOptionItem.createNew("yes", "Yes");
        AnswerOptionItem rehydrated = AnswerOptionItem.rehydrate("no", "No");

        assertEquals("yes", created.value());
        assertEquals("Yes", created.label());

        assertEquals("no", rehydrated.value());
        assertEquals("No", rehydrated.label());
    }

}




