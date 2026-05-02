package com.acme.orderquestionnaire.domain.unit.domain.questionnaire;

import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionValidationFailure;
import com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerConfigurationFactory;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.CompositeCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.EqualCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.NumericCondition;
import com.acme.orderquestionnaire.domain.questionnaire.tree.ConfiguredQuestionTreeNode;
import com.acme.orderquestionnaire.domain.unit.testutils.mocks.audit.AuditTestData;
import com.acme.orderquestionnaire.domain.unit.testutils.mocks.question.QuestionMock;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("ConfiguredQuestion")
class ConfiguredQuestionTest {


    public static final Question QUESTION_1 = QuestionMock.DEFAULT_QUESTION_1.getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

    @Test
    @DisplayName("When question is hidden then validation should succeed without answer")
    void shouldSkipValidationWhenHidden() {
        ConfiguredQuestion configuredQuestion = ConfiguredQuestion.createNew(
                QUESTION_1,
                AnswerConfigurationFactory.createTextStrategy(),
                1
        );
        configuredQuestion.rootCondition(new NumericCondition("other", 10, ">"));

        Result<Void, QuestionValidationFailure> validation = configuredQuestion.validate(Map.of("other", 1));
        assertInstanceOf(Result.Success.class, validation);
    }

    @Test
    @DisplayName("When visible and answer is missing then failure should include question context")
    void shouldReturnQuestionContextOnFailure() {
        ConfiguredQuestion configuredQuestion = ConfiguredQuestion.createNew(
                QUESTION_1,
                AnswerConfigurationFactory.createTextStrategy(),
                3
        );

        Result<Void, QuestionValidationFailure> validation = configuredQuestion.validate(new HashMap<>());
        assertInstanceOf(Result.Failure.class, validation);
        var failure = (Result.Failure<Void, QuestionValidationFailure>) validation;
        assertEquals("how_satisfied_are_you", failure.error().questionId());
        assertEquals("How satisfied are you with our service?", failure.error().questionLabel());
        assertEquals(3, failure.error().order());
    }

    @Test
    @DisplayName("When exporting to tree node then condition should be present")
    void shouldExportToTreeNode() {
        ConfiguredQuestion configuredQuestion = ConfiguredQuestion.createNew(
                QUESTION_1,
                AnswerConfigurationFactory.createNumberStrategy(),
                0
        );
        configuredQuestion.rootCondition(new NumericCondition("root", 1, "=="));

        ConfiguredQuestionTreeNode node = configuredQuestion.toTreeNode();
        assertEquals("how_satisfied_are_you", node.question().id());
        assertEquals("NUMBER", node.answerConfiguration().type().name());
        assertEquals("NUMERIC", node.condition().type());
    }

    // ── Question status ───────────────────────────────────────────────────────

    @Test
    @DisplayName("When question is DRAFT and answer is provided then QUESTION_NOT_ACTIVE violation is returned")
    void shouldReturnQuestionNotActiveWhenAnswerProvidedForDraftQuestion() {
        Question draftQuestion = Question.rehydrate("q_inactive", "Inactive Q",
                ParameterizationStatus.DRAFT, "SKU", AuditTestData.createdAudit());
        ConfiguredQuestion cq = ConfiguredQuestion.createNew(
                draftQuestion, AnswerConfigurationFactory.createTextStrategy(), 1);

        Result<Void, QuestionValidationFailure> result = cq.validate(Map.of("q_inactive", "some_value"));

        assertInstanceOf(Result.Failure.class, result);
        var failure = (Result.Failure<Void, QuestionValidationFailure>) result;
        assertTrue(failure.error().errors().stream()
                .anyMatch(e -> "QUESTION_NOT_ACTIVE".equals(e.code())));
    }

    @Test
    @DisplayName("When question is DRAFT and no answer is provided then validation succeeds")
    void shouldSucceedWhenDraftQuestionHasNoAnswer() {
        Question draftQuestion = Question.rehydrate("q_inactive", "Inactive Q",
                ParameterizationStatus.DRAFT, "SKU", AuditTestData.createdAudit());
        ConfiguredQuestion cq = ConfiguredQuestion.createNew(
                draftQuestion, AnswerConfigurationFactory.createTextStrategy(), 1);

        Result<Void, QuestionValidationFailure> result = cq.validate(Map.of());

        assertInstanceOf(Result.Success.class, result);
    }

    // ── Condition references inactive question ────────────────────────────────

    @Test
    @DisplayName("When condition references a DRAFT question then CONDITION_REFERENCED_QUESTION_NOT_ACTIVE is returned")
    void shouldReturnConditionRefInactiveWhenReferencedQuestionIsDraft() {
        Question activeQ = Question.rehydrate("q_conditional", "Conditional",
                ParameterizationStatus.ACTIVE, "SKU", AuditTestData.createdAudit());
        ConfiguredQuestion cq = ConfiguredQuestion.createNew(
                activeQ, AnswerConfigurationFactory.createTextStrategy(), 1);
        cq.rootCondition(new EqualCondition("q_root", "YES"));

        Map<String, ParameterizationStatus> statuses = Map.of(
                "q_root", ParameterizationStatus.DRAFT
        );

        Result<Void, QuestionValidationFailure> result = cq.validate(Map.of(), statuses);

        assertInstanceOf(Result.Failure.class, result);
        var failure = (Result.Failure<Void, QuestionValidationFailure>) result;
        assertTrue(failure.error().errors().stream()
                .anyMatch(e -> "CONDITION_REFERENCED_QUESTION_NOT_ACTIVE".equals(e.code())));
    }

    @Test
    @DisplayName("When composite OR condition references inactive questions then violation is emitted")
    void shouldReturnConditionRefInactiveForCompositeOrCondition() {
        Question activeQ = Question.rehydrate("q_approval", "Approval",
                ParameterizationStatus.ACTIVE, "SKU", AuditTestData.createdAudit());
        ConfiguredQuestion cq = ConfiguredQuestion.createNew(
                activeQ, AnswerConfigurationFactory.createTextStrategy(), 1);

        CompositeCondition composite = new CompositeCondition(false); // OR
        composite.addCondition(new EqualCondition("q_mgr", "APPROVED"));
        composite.addCondition(new EqualCondition("q_dir", "APPROVED"));
        cq.rootCondition(composite);

        Map<String, ParameterizationStatus> statuses = Map.of(
                "q_mgr", ParameterizationStatus.INACTIVE,
                "q_dir", ParameterizationStatus.DRAFT
        );

        Result<Void, QuestionValidationFailure> result = cq.validate(Map.of(), statuses);

        assertInstanceOf(Result.Failure.class, result);
        var failure = (Result.Failure<Void, QuestionValidationFailure>) result;
        List<DomainError> errors = failure.error().errors();
        assertTrue(errors.stream().anyMatch(e -> "CONDITION_REFERENCED_QUESTION_NOT_ACTIVE".equals(e.code())));
        assertTrue(errors.stream()
                .filter(e -> "CONDITION_REFERENCED_QUESTION_NOT_ACTIVE".equals(e.code()))
                .anyMatch(e -> e.message().contains("q_mgr") || e.message().contains("q_dir")));
    }

    @Test
    @DisplayName("When condition references all active questions then validation proceeds normally")
    void shouldNotReportConditionRefInactiveWhenAllRefsAreActive() {
        Question activeQ = Question.rehydrate("q_conditional", "Conditional",
                ParameterizationStatus.ACTIVE, "SKU", AuditTestData.createdAudit());
        ConfiguredQuestion cq = ConfiguredQuestion.createNew(
                activeQ, AnswerConfigurationFactory.createTextStrategy(), 1);
        cq.rootCondition(new EqualCondition("q_root", "YES"));

        // All referenced questions are ACTIVE → condition evaluated normally
        Map<String, ParameterizationStatus> statuses = Map.of("q_root", ParameterizationStatus.ACTIVE);

        // q_root = YES → condition satisfied → q_conditional is visible but no answer → MANDATORY_ANSWER
        Result<Void, QuestionValidationFailure> result = cq.validate(Map.of("q_root", "YES"), statuses);

        assertInstanceOf(Result.Failure.class, result);
        var failure = (Result.Failure<Void, QuestionValidationFailure>) result;
        assertTrue(failure.error().errors().stream()
                .anyMatch(e -> "MANDATORY_ANSWER".equals(e.code())));
    }
}
