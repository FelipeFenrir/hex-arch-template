package com.acme.orderquestionnaire.domain.unit.domain.questionnaire;

import com.acme.orderquestionnaire.domain.question.QuestionFactory;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestionFactory;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireFactory;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.NumericCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.QuestionConditionComposer;
import com.acme.orderquestionnaire.domain.unit.testutils.mocks.audit.AuditTestData;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("QuestionnaireFactory mutation tests")
class QuestionnaireFactoryMutationTest {

    @Test
    @DisplayName("combineConditions returns null for empty conditions")
    void shouldReturnNullForEmptyConditions() {
        var result = QuestionConditionComposer.combineConditions(true);
        assertNull(result);
    }

    @Test
    @DisplayName("composeWithAnd enforces both conditions")
    void shouldComposeWithAnd() {
        var condition = QuestionConditionComposer.composeWithAnd(
                new NumericCondition("q_one", 5, ">"),
                new NumericCondition("q_two", 3, "<=")
        );

        assertNotNull(condition);
        assertTrue(condition.isSatisfy(Map.of("q_one", 6, "q_two", 3)));
        assertFalse(condition.isSatisfy(Map.of("q_one", 6, "q_two", 4)));
        assertFalse(condition.isSatisfy(Map.of("q_one", 4, "q_two", 3)));
    }

    @Test
    @DisplayName("composeWithOr satisfies when any condition matches")
    void shouldComposeWithOr() {
        var condition = QuestionConditionComposer.composeWithOr(
                new NumericCondition("q_one", 10, ">"),
                new NumericCondition("q_two", 2, "==")
        );

        assertNotNull(condition);
        assertTrue(condition.isSatisfy(Map.of("q_one", 11, "q_two", 0)));
        assertTrue(condition.isSatisfy(Map.of("q_one", 0, "q_two", 2)));
        assertFalse(condition.isSatisfy(Map.of("q_one", 0, "q_two", 3)));
    }

    @Test
    @DisplayName("builder propagates question result failures")
    void shouldPropagateQuestionResultFailures() {
        var validQuestion = unwrapQuestion(unwrapQuestionBuilder(
                QuestionFactory.createNew("q_one", "Question 1", "SALE", AuditTestData.createdAudit())
        ).build());

        Result<com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion, List<DomainError>> invalidConfiguredQuestion =
                Result.failure(List.of(new DomainError("FORCED_FAILURE", "forced")));

        var questionnaireResult = unwrapQuestionnaireBuilder(
                QuestionnaireFactory.createNew("survey", "channel", "journey", "desc", AuditTestData.createdAudit())
        )
                .withQuestion(unwrapConfiguredQuestion(unwrapConfiguredBuilder(ConfiguredQuestionFactory.from(validQuestion)).asText()))
                .withQuestion(invalidConfiguredQuestion)
                .build();

        assertInstanceOf(Result.Failure.class, questionnaireResult);
        var failure = (Result.Failure<com.acme.orderquestionnaire.domain.questionnaire.Questionnaire, List<DomainError>>) questionnaireResult;
        assertFalse(failure.error().isEmpty());
    }

    @Test
    @DisplayName("createNew with all nulls accumulates id, channel, journey, description and auditInfo errors")
    void shouldAccumulateAllErrorsWhenAllNullsOnCreateNew() {
        var result = QuestionnaireFactory.createNew(null, null, null, null, null);

        assertInstanceOf(Result.Failure.class, result);
        var failure = (Result.Failure<QuestionnaireFactory.QuestionnaireBuilder, List<DomainError>>) result;
        assertEquals(5, failure.error().size());
    }

    @Test
    @DisplayName("rehydrate with all nulls accumulates id, channel, journey, description, status and auditInfo errors")
    void shouldAccumulateAllErrorsWhenAllNullsOnRehydrate() {
        var result = QuestionnaireFactory.rehydrate(null, null, null, null, null, null);

        assertInstanceOf(Result.Failure.class, result);
        var failure = (Result.Failure<QuestionnaireFactory.QuestionnaireBuilder, List<DomainError>>) result;
        assertEquals(6, failure.error().size());
    }

    private static QuestionFactory.NewQuestionBuilder unwrapQuestionBuilder(
            Result<QuestionFactory.NewQuestionBuilder, List<DomainError>> result
    ) {
        if (result instanceof Result.Success<QuestionFactory.NewQuestionBuilder, List<DomainError>>(QuestionFactory.NewQuestionBuilder value)) {
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

    private static ConfiguredQuestionFactory.ConfiguredQuestionBuilder unwrapConfiguredBuilder(
            Result<ConfiguredQuestionFactory.ConfiguredQuestionBuilder, List<DomainError>> result
    ) {
        if (result instanceof Result.Success<ConfiguredQuestionFactory.ConfiguredQuestionBuilder, List<DomainError>>(ConfiguredQuestionFactory.ConfiguredQuestionBuilder value)) {
            return value;
        }
        throw new AssertionError("Expected success configured builder");
    }

    private static com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion unwrapConfiguredQuestion(
            Result<com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion, List<DomainError>> result
    ) {
        if (result instanceof Result.Success<com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion, List<DomainError>>(com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion value)) {
            return value;
        }
        throw new AssertionError("Expected success configured question");
    }

    private static QuestionnaireFactory.QuestionnaireBuilder unwrapQuestionnaireBuilder(
            Result<QuestionnaireFactory.QuestionnaireBuilder, List<DomainError>> result
    ) {
        if (result instanceof Result.Success<QuestionnaireFactory.QuestionnaireBuilder, List<DomainError>>(QuestionnaireFactory.QuestionnaireBuilder value)) {
            return value;
        }
        throw new AssertionError("Expected success questionnaire builder");
    }
}




