package com.acme.orderquestionnaire.domain.unit.domain.question;

import com.acme.orderquestionnaire.domain.question.QuestionFactory;
import com.acme.orderquestionnaire.domain.unit.testutils.mocks.audit.AuditTestData;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@UnitTest
@DisplayName("QuestionFactory mutation tests")
class QuestionFactoryMutationTest {

    @Test
    @DisplayName("createNew accumulates both required field errors")
    void shouldAccumulateRequiredFieldErrorsOnCreateNew() {
        var result = QuestionFactory.createNew(" ", " ", "SALE", AuditTestData.createdAudit());

        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure"));
        assertEquals(2, errors.size());
        assertEquals("REQUIRED_FIELD", errors.getFirst().code());
    }

    @Test
    @DisplayName("createNew fails when sales item reference code is blank")
    void shouldFailWhenSalesItemReferenceCodeIsBlank() {
        var result = QuestionFactory.createNew("q_one", "Question", " ", AuditTestData.createdAudit());
        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure"));
        assertEquals(1, errors.size());
        assertEquals("salesItemReferenceCode must not be blank", errors.getFirst().message());
    }

    @Test
    @DisplayName("rehydrate fails when status is null")
    void shouldFailWhenStatusIsNull() {
        var result = QuestionFactory.rehydrate("q-1", "Question", null, "SALE", AuditTestData.createdAudit());
        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure"));
        assertEquals(1, errors.size());
        assertEquals("REQUIRED_OBJECT", errors.getFirst().code());
    }

    @Test
    @DisplayName("rehydrate keeps provided status on success")
    void shouldKeepProvidedStatusOnRehydrate() {
        var question = unwrapQuestion(unwrapRehydratedBuilder(
                QuestionFactory.rehydrate("q-2", "Question 2", ParameterizationStatus.ACTIVE, "SALE", AuditTestData.createdAudit())
        ).build());
        assertEquals(ParameterizationStatus.ACTIVE, question.status());
    }

    @Test
    @DisplayName("createNew with blank id and null auditInfo accumulates two errors")
    void shouldAccumulateErrorsWhenIdBlankAndAuditInfoNull() {
        var result = QuestionFactory.createNew(" ", "Label", "SALE", null);
        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure"));
        assertEquals(2, errors.size());
    }

    @Test
    @DisplayName("rehydrate with null status and null auditInfo accumulates two errors")
    void shouldAccumulateErrorsWhenStatusAndAuditInfoNull() {
        var result = QuestionFactory.rehydrate("q_one", "Label", null, "SALE", null);
        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure"));
        assertEquals(2, errors.size());
    }

    private static QuestionFactory.NewQuestionBuilder unwrapBuilder(
            Result<QuestionFactory.NewQuestionBuilder, List<DomainError>> result
    ) {
        if (result instanceof Result.Success<QuestionFactory.NewQuestionBuilder, List<DomainError>>(QuestionFactory.NewQuestionBuilder value)) {
            return value;
        }
        throw new AssertionError("Expected success builder");
    }

    private static QuestionFactory.RehydratedQuestionBuilder unwrapRehydratedBuilder(
            Result<QuestionFactory.RehydratedQuestionBuilder, List<DomainError>> result
    ) {
        if (result instanceof Result.Success<QuestionFactory.RehydratedQuestionBuilder, List<DomainError>>(QuestionFactory.RehydratedQuestionBuilder value)) {
            return value;
        }
        throw new AssertionError("Expected success builder");
    }

    private static com.acme.orderquestionnaire.domain.question.Question unwrapQuestion(
            Result<com.acme.orderquestionnaire.domain.question.Question, List<DomainError>> result
    ) {
        if (result instanceof Result.Success<com.acme.orderquestionnaire.domain.question.Question, List<DomainError>>(com.acme.orderquestionnaire.domain.question.Question value)) {
            return value;
        }
        throw new AssertionError("Expected success question");
    }
}


