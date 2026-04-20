package com.acme.orderquestionnaire.domain.questionnaire;

import com.acme.orderquestionnaire.domain.questionnaire.conditioner.NumericCondition;
import com.acme.orderquestionnaire.domain.questionnaire.tree.QuestionnaireTree;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.testutils.mocks.audit.AuditTestData;
import com.acme.orderquestionnaire.testutils.mocks.question.QuestionMock;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("QuestionnaireFactory")
class QuestionnaireFactoryTest {

    private static final Question QUESTION_1 = QuestionMock.DEFAULT_QUESTION_1
            .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));
    private static final Question QUESTION_2 = QuestionMock.DEFAULT_QUESTION_2
            .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

    @Test
    @DisplayName("When creating a new questionnaire, it should have DRAFT status")
    void shouldCreateNewQuestionnaireWithDraftStatus() {
        var questionnaire = QuestionnaireFactory
                .createNew("survey_one", "channel_one", "journey_one", "description", AuditTestData.createdAudit())
                .flatMap(QuestionnaireFactory.QuestionnaireBuilder::build)
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        assertEquals("survey_one", questionnaire.id());
        assertEquals("channel_one", questionnaire.channelDistributionId());
        assertEquals("journey_one", questionnaire.journeyDistributionId());
        assertEquals("description", questionnaire.description());
        assertEquals(ParameterizationStatus.DRAFT, questionnaire.status());
    }

    @Test
    @DisplayName("When rehydrating a questionnaire, it should preserve the status")
    void shouldRehydrateQuestionnaireWithStatus() {
        var questionnaire = QuestionnaireFactory
                .rehydrate("survey_one", "channel_one", "journey_one",
                        "description", ParameterizationStatus.ACTIVE, AuditTestData.createdAudit())
                .flatMap(QuestionnaireFactory.QuestionnaireBuilder::build)
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        assertEquals("survey_one", questionnaire.id());
        assertEquals(ParameterizationStatus.ACTIVE, questionnaire.status());
    }

    @Test
    @DisplayName("When creating invalid questionnaire, it should return accumulated errors")
    void shouldReturnFailureForInvalidCreate() {
        var result = QuestionnaireFactory.createNew(" ", " ", null, "", AuditTestData.createdAudit());

        assertTrue(result.isFailure());
        List<DomainError> error = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertEquals(4, error.size());
    }

    @Test
    @DisplayName("When questionnaire id is not snake_case, it should return invalid id format")
    void shouldReturnFailureForInvalidIdFormat() {
        var result = QuestionnaireFactory.createNew("MyQuestionnaire", "channel", "journey", "desc", AuditTestData.createdAudit());

        assertTrue(result.isFailure());
        List<DomainError> error = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertTrue(error.stream().anyMatch(domainError -> "INVALID_ID_FORMAT".equals(domainError.code())));
    }

    @Test
    @DisplayName("When adding Result-configured questions, builder should collect all valid ones")
    void shouldAddQuestionResultToQuestionnaire() {
        var questionResult = ConfiguredQuestionFactory
                .from(QUESTION_1)
                .flatMap(ConfiguredQuestionFactory.QuestionBuilder::asText);

        var questionnaire = QuestionnaireFactory
                .createNew("survey_one", "channel_one", "journey_one", "desc", AuditTestData.createdAudit())
                .flatMap(builder -> builder.withQuestion(questionResult).build())
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        assertEquals(1, questionnaire.getOrderedQuestions().size());
    }

    @Test
    @DisplayName("When composing conditions fluently with AND/OR, it should handle mixed operators")
    void shouldComposeConditionsWithMixedOperators() {
        var condition = QuestionnaireFactory
                .condition(new NumericCondition("q1", 5, ">"))
                .and(new NumericCondition("q1", 10, "<="))
                .or(new NumericCondition("q2", 3, "=="))
                .build();

        assertNotNull(condition);

        Map<String, Object> answers = new HashMap<>();
        answers.put("q1", 8);
        answers.put("q2", 99);
        assertTrue(condition.isSatisfy(answers));

        answers.put("q1", 12);
        answers.put("q2", 3);
        assertTrue(condition.isSatisfy(answers));

        answers.put("q1", 12);
        answers.put("q2", 99);
        assertFalse(condition.isSatisfy(answers));
    }

    @Test
    @DisplayName("When validating answers, should accumulate errors across visible questions")
    void shouldAccumulateValidationErrors() {
        var q1 = ConfiguredQuestionFactory.from(QUESTION_1)
                .flatMap(ConfiguredQuestionFactory.QuestionBuilder::asNumber)
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));
        var q2 = ConfiguredQuestionFactory.from(QUESTION_2)
                .flatMap(ConfiguredQuestionFactory.QuestionBuilder::asText)
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        var questionnaire = QuestionnaireFactory
                .createNew("survey_id", "channel_id", "journey_id",
                        "Customer Satisfaction Survey", AuditTestData.createdAudit())
                .flatMap(builder -> builder
                        .withQuestion(q1)
                        .withQuestion(q2)
                        .build())
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        var validation = questionnaire.answerValidation(Map.of());
        assertTrue(validation.isFailure());
        List<QuestionValidationFailure> error = validation.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertEquals(2, error.size());
        assertEquals("how_satisfied_are_you", error.getFirst().questionId());
        assertEquals(0, error.getFirst().order());
    }

    @Test
    @DisplayName("When exporting questionnaire tree, it should contain question, rule and condition")
    void shouldExportTree() {
        var condition = QuestionnaireFactory.condition(new NumericCondition("how_satisfied_are_you",
                7, ">=")).build();

        var configuredQuestion = ConfiguredQuestionFactory.from(QUESTION_1)
                .flatMap(builder -> builder
                        .withOrder(2)
                        .withCondition(condition)
                        .asNumber())
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        var questionnaire = QuestionnaireFactory
                .createNew("survey_id", "channel", "journey", "desc", AuditTestData.createdAudit())
                .flatMap(builder -> builder.withQuestion(configuredQuestion).build())
                .getOrElseThrow(error -> new IllegalStateException("Expected success but got failure: " + error));

        QuestionnaireTree tree = questionnaire.toTree();
        assertEquals("survey_id", tree.id());
        assertEquals(1, tree.questions().size());
        assertEquals("how_satisfied_are_you", tree.questions().getFirst().question().id());
        assertEquals("NUMBER", tree.questions().getFirst().answerConfiguration().type().name());
        assertNotNull(tree.questions().getFirst().condition());
        assertEquals(1, tree.questions().getFirst().condition().children().size());
    }

    @Test
    @DisplayName("When creating with null auditInfo, should return REQUIRED_OBJECT error")
    void shouldRejectNullAuditInfoOnCreateNew() {
        var result = QuestionnaireFactory.createNew("survey_one", "channel_one", "journey_one", "desc", null);

        assertTrue(result.isFailure());
        List<DomainError> error = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertEquals(1, error.size());
        assertEquals("REQUIRED_OBJECT", error.getFirst().code());
    }

    @Test
    @DisplayName("When rehydrating with null auditInfo, should return REQUIRED_OBJECT error")
    void shouldRejectNullAuditInfoOnRehydrate() {
        var result = QuestionnaireFactory.rehydrate("survey_one", "channel_one", "journey_one", "desc",
                ParameterizationStatus.ACTIVE, null);

        assertTrue(result.isFailure());
        List<DomainError> error = result.errorOrElseThrow(() -> new IllegalStateException("Expected failure"));
        assertEquals(1, error.size());
        assertEquals("REQUIRED_OBJECT", error.getFirst().code());
    }
}
