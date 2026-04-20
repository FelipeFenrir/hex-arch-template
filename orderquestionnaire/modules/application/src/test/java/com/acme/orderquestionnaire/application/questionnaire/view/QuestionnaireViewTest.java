package com.acme.orderquestionnaire.application.questionnaire.view;

import com.acme.orderquestionnaire.application.audit.dto.queries.SearchByAuditInfo;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.CreateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.GetQuestionnaireById;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.SearchQuestionnaireByFilter;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionAnswerValidationView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionAnswerViolationView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.ValidateQuestionnaireAnswersView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.AnswerConfigurationView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionConditionView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionConfigurationView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireCreatedView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireUpdatedView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireView;
import com.acme.orderquestionnaire.application.questionnaire.service.support.ConditionQuestionIdExtractor;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.question.answer.AnswerOptionItem;
import com.acme.orderquestionnaire.domain.question.answer.strategy.AnswerConfigurationFactory;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.EqualCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.NumericCondition;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@DisplayName("Questionnaire view mappings and DTOs")
class QuestionnaireViewTest {

    private static final Id USER_ID = Id.withId("00000000-0000-0000-0000-000000000001");
    private static final LocalDateTime NOW = LocalDateTime.parse("2026-01-01T10:00:00");

    // ── QuestionnaireCreatedView ───────────────────────────────────────────────

    @Test
    @DisplayName("QuestionnaireCreatedView.from: should map all fields from Questionnaire")
    void shouldMapQuestionnaireCreatedView() {
        Questionnaire q = buildQuestionnaire();
        QuestionnaireCreatedView view = QuestionnaireCreatedView.from(q);

        assertEquals("q_001", view.id());
        assertEquals("APP", view.channelDistributionId());
        assertEquals("JOURNEY", view.journeyDistributionId());
        assertEquals("Test questionnaire", view.description());
        assertEquals("DRAFT", view.status());
        assertNotNull(view.createdBy());
        assertEquals(NOW, view.createdAt());
    }

    @Test
    @DisplayName("QuestionnaireUpdatedView.from: should map configured questions, answer config and dependencies")
    void shouldMapQuestionnaireUpdatedViewWithConfiguredQuestions() {
        Question dependencyQuestion = buildQuestion("q_dependency");
        ConfiguredQuestion dependencyConfigured = ConfiguredQuestion.createNew(
                dependencyQuestion,
                AnswerConfigurationFactory.createTextStrategy(),
                1);

        Question conditionalQuestion = buildQuestion("q_income");
        ConfiguredQuestion conditionalConfigured = ConfiguredQuestion.createNew(
                conditionalQuestion,
                AnswerConfigurationFactory.createNumberStrategy(),
                2);
        conditionalConfigured.rootCondition(new EqualCondition("q_dependency", "yes"));

        Questionnaire questionnaire = Questionnaire.rehydrate(
                QuestionnaireId.of("q_001", "APP", "JOURNEY"),
                "Test questionnaire",
                ParameterizationStatus.ACTIVE,
                List.of(dependencyConfigured, conditionalConfigured),
                OrderQuestionnaireAuditFactory.createNew(USER_ID, "REF-1", "Test User", "test@acme.com", NOW)
                        .getOrElseThrow(error -> new IllegalStateException("Invalid audit test data: " + error))
        );

        QuestionnaireUpdatedView view = QuestionnaireUpdatedView.from(questionnaire);

        assertEquals("ACTIVE", view.status());
        assertEquals(2, view.configuredQuestions().size());
        assertEquals("q_income", view.configuredQuestions().get(1).questionId());
        assertEquals("NUMBER", view.configuredQuestions().get(1).answerConfiguration().type());
        assertEquals("EQUAL", view.configuredQuestions().get(1).rootCondition().type());
        assertTrue(view.configuredQuestions().get(1).dependsOnQuestionIds().contains("q_dependency"));
    }

    // ── QuestionConditionView ─────────────────────────────────────────────────

    @Test
    @DisplayName("QuestionConditionView.from(null QuestionCondition): should return null")
    void conditionViewFromNullCondition() {
        assertNull(QuestionConditionView.from((com.acme.orderquestionnaire.domain.questionnaire.conditioner.QuestionCondition) null));
    }

    @Test
    @DisplayName("QuestionConditionView.from(null TreeNode): should return null")
    void conditionViewFromNullTreeNode() {
        assertNull(QuestionConditionView.from((com.acme.orderquestionnaire.domain.questionnaire.tree.QuestionConditionTreeNode) null));
    }

    @Test
    @DisplayName("QuestionConditionView.from: should map condition tree recursively")
    void conditionViewShouldMapTreeRecursively() {
        var condition = new EqualCondition("q_age", "yes");
        QuestionConditionView view = QuestionConditionView.from(condition);

        assertNotNull(view);
        assertEquals("EQUAL", view.type());
        assertEquals("q_age", view.attributes().get("questionRootCode"));
        assertTrue(view.children().isEmpty());
    }

    // ── AnswerConfigurationView ───────────────────────────────────────────────

    @Test
    @DisplayName("AnswerConfigurationView.from(null AnswerConfiguration): should return null")
    void answerViewFromNullAnswerConfig() {
        assertNull(AnswerConfigurationView.from((com.acme.orderquestionnaire.domain.question.answer.AnswerConfiguration) null));
    }

    @Test
    @DisplayName("AnswerConfigurationView.from(null TreeNode): should return null")
    void answerViewFromNullTreeNode() {
        assertNull(AnswerConfigurationView.from((com.acme.orderquestionnaire.domain.questionnaire.tree.AnswerConfigurationTreeNode) null));
    }

    @Test
    @DisplayName("AnswerConfigurationView.from: should map answer configuration to view")
    void answerViewShouldMapStrategy() {
        var strategy = AnswerConfigurationFactory.createNumberStrategy();
        AnswerConfigurationView view = AnswerConfigurationView.from(strategy);

        assertNotNull(view);
        assertEquals("NUMBER", view.type());
        assertNotNull(view.attributes());
    }

    @Test
    @DisplayName("AnswerConfigurationView.from option list: should expose only value and label for options")
    void answerViewShouldNotExposeOptionStatus() {
        var strategy = AnswerConfigurationFactory.createListStrategy(
                List.of(AnswerOptionItem.rehydrate("yes", "Yes"))
        );

        AnswerConfigurationView view = AnswerConfigurationView.from(strategy);

        assertNotNull(view);
        assertEquals("OPTION_LIST", view.type());
        var options = (List<?>) view.attributes().get("answerOptions");
        assertEquals(1, options.size());
        Object optionNode = options.getFirst();
        assertEquals("AnswerOptionTreeNode", optionNode.getClass().getSimpleName());
        assertEquals(2, optionNode.getClass().getRecordComponents().length);
    }

    // ── QuestionConfigurationView ─────────────────────────────────────────────

    @Test
    @DisplayName("QuestionConfigurationView.from(null): should return null")
    void configViewFromNull() {
        assertNull(QuestionConfigurationView.from(null));
    }

    @Test
    @DisplayName("QuestionConfigurationView.from: should map configured question")
    void configViewShouldMap() {
        Question question = buildQuestion("q_age");
        ConfiguredQuestion cq = ConfiguredQuestion.createNew(
                question, AnswerConfigurationFactory.createNumberStrategy(), 2);

        QuestionConfigurationView view = QuestionConfigurationView.from(cq);

        assertNotNull(view);
        assertEquals(2, view.order());
        assertNotNull(view.answerConfiguration());
        assertNull(view.rootCondition());
    }

    // ── ConditionQuestionIdExtractor ──────────────────────────────────────────

    @Test
    @DisplayName("ConditionQuestionIdExtractor.extract(null): should return empty set")
    void extractorShouldReturnEmptyForNull() {
        assertTrue(ConditionQuestionIdExtractor.extract(null).isEmpty());
    }

    @Test
    @DisplayName("ConditionQuestionIdExtractor.extract: should collect ids from composite condition")
    void extractorShouldCollectFromComposite() {
        var composite = new com.acme.orderquestionnaire.domain.questionnaire.conditioner.CompositeCondition(true);
        composite.addCondition(new EqualCondition("q_one", "yes"));
        composite.addCondition(new NumericCondition("q_two", 10, ">"));

        Set<String> ids = ConditionQuestionIdExtractor.extract(composite);
        assertTrue(ids.contains("q_one"));
        assertTrue(ids.contains("q_two"));
    }

    // ── Plain record instantiation (exercises canonical constructors) ──────────

    @Test
    @DisplayName("GetQuestionnaireById record should expose questionnaireId")
    void getQuestionnaireByIdRecord() {
        var id = QuestionnaireId.of("q_001", "APP", "JOURNEY");
        var query = new GetQuestionnaireById(id);
        assertEquals(id, query.questionnaireId());
    }

    @Test
    @DisplayName("SearchQuestionnaireByFilter record should be constructed")
    void searchQuestionnaireByFilterRecord() {
        var filter = new SearchQuestionnaireByFilter(
                List.of("q_001"),
                null,
                null,
                "Survey",
                null,
                null,
                HybridPageRequest.ofPage(0, 10, null)
        );
        assertEquals(List.of("q_001"), filter.ids());
        assertEquals("Survey", filter.descriptionContains());
    }

    @Test
    @DisplayName("SearchByAuditInfo record should be constructed")
    void searchByAuditInfoRecord() {
        var audit = new SearchByAuditInfo("uid", "REF", null, null, null, null, null, null, null, null, null, null);
        assertEquals("uid", audit.createdById());
        assertEquals("REF", audit.createdByReferenceCode());
    }

    @Test
    @DisplayName("CreateQuestionnaireCommand record should be constructed")
    void createQuestionnaireCommandRecord() {
        var user = new AuditUserParam(USER_ID, "REF", "Name", "email@acme.com");
        var cmd = new CreateQuestionnaireCommand("q_001", "APP", "JOURNEY", "desc", user, NOW);
        assertEquals("q_001", cmd.id());
        assertEquals("desc", cmd.description());
    }

    @Test
    @DisplayName("QuestionnaireView record should be constructed with all fields")
    void questionnaireViewRecord() {
        var qId = QuestionnaireId.of("q_001", "APP", "JOURNEY");
        var view = new QuestionnaireView(
                qId, "description", "DRAFT", List.of(), null, NOW, null, null);
        assertEquals(qId, view.questionnaireId());
        assertEquals("DRAFT", view.status());
    }

    // ── Validation views ──────────────────────────────────────────────────────

    @Test
    @DisplayName("QuestionAnswerViolationView record should be constructed with all fields")
    void questionAnswerViolationViewRecord() {
        var view = new QuestionAnswerViolationView(
                "VALUE_BELOW_MIN",
                "Value below minimum.",
                "ANSWER_CONFIGURATION",
                "NUMBER_RANGE",
                java.util.Map.of("min", 0.0),
                "answerConfiguration"
        );
        assertEquals("VALUE_BELOW_MIN", view.code());
        assertEquals("Value below minimum.", view.message());
        assertEquals("ANSWER_CONFIGURATION", view.source());
        assertEquals("NUMBER_RANGE", view.ruleType());
        assertEquals("answerConfiguration", view.rulePath());
    }

    @Test
    @DisplayName("QuestionAnswerValidationView record should be constructed with all fields")
    void questionAnswerValidationViewRecord() {
        var answerRule = new AnswerConfigurationView("NUMBER", java.util.Map.of("min", 0.0));
        var violation  = new QuestionAnswerViolationView(
                "VALUE_BELOW_MIN",
                "Value below minimum.",
                "ANSWER_CONFIGURATION",
                "NUMBER_RANGE",
                java.util.Map.of("min", 0.0),
                "answerConfiguration"
        );
        var view = new QuestionAnswerValidationView(
                "q_income", "Income", 1, -10.0, true, answerRule, null, Set.of(), List.of(violation));

        assertEquals("q_income", view.questionId());
        assertEquals("Income", view.questionLabel());
        assertEquals(1, view.order());
        assertEquals(-10.0, view.providedAnswer());
        assertTrue(view.visibleByCondition());
        assertNotNull(view.answerRule());
        assertEquals("NUMBER", view.answerRule().type());
        assertNull(view.conditionRule());
        assertTrue(view.dependsOnQuestionIds().isEmpty());
        assertEquals(1, view.violations().size());
        assertEquals("VALUE_BELOW_MIN", view.violations().getFirst().code());
    }

    @Test
    @DisplayName("ValidateQuestionnaireAnswersView record should expose valid flag and violationsByQuestionId")
    void validateQuestionnaireAnswersViewRecord() {
        var view = new ValidateQuestionnaireAnswersView("q_001", "APP", "JOURNEY_01", true, java.util.Map.of());
        assertEquals("q_001", view.questionnaireId());
        assertEquals("APP", view.channelDistributionId());
        assertEquals("JOURNEY_01", view.journeyDistributionId());
        assertTrue(view.valid());
        assertTrue(view.violationsByQuestionId().isEmpty());
    }

    @Test
    @DisplayName("ValidateQuestionnaireAnswersView record should hold violations when invalid")
    void validateQuestionnaireAnswersViewRecordWithViolations() {
        var answerRule = new AnswerConfigurationView("TEXT", java.util.Map.of("regexPattern", "^[A-Z]+$"));
        var violation  = new QuestionAnswerViolationView(
                "PATTERN_MISMATCH",
                "Pattern not matched.",
                "ANSWER_CONFIGURATION",
                "TEXT_PATTERN",
                java.util.Map.of("regexPattern", "^[A-Z]+$"),
                "answerConfiguration"
        );
        var qView = new QuestionAnswerValidationView(
                "q_name", "Name", 1, "lowercase", true, answerRule, null, Set.of(), List.of(violation));
        var view = new ValidateQuestionnaireAnswersView(
                "q_001", "APP", "JOURNEY_01", false, java.util.Map.of("q_name", qView));

        assertFalse(view.valid());
        assertEquals(1, view.violationsByQuestionId().size());
        assertNotNull(view.violationsByQuestionId().get("q_name"));
        assertEquals("PATTERN_MISMATCH",
                view.violationsByQuestionId().get("q_name").violations().getFirst().code());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Questionnaire buildQuestionnaire() {
        return Questionnaire.rehydrate(
                QuestionnaireId.of("q_001", "APP", "JOURNEY"),
                "Test questionnaire", ParameterizationStatus.DRAFT, List.of(),
                OrderQuestionnaireAuditFactory.createNew(USER_ID, "REF-1", "Test User", "test@acme.com", NOW)
                        .getOrElseThrow(error -> new IllegalStateException("Invalid audit test data: " + error)));
    }

    private static Question buildQuestion(String id) {
        return Question.rehydrate(id, "Label " + id, ParameterizationStatus.ACTIVE, "SKU-1",
                OrderQuestionnaireAuditFactory.createNew(USER_ID, "REF-1", "Test User", "test@acme.com", NOW)
                        .getOrElseThrow(error -> new IllegalStateException("Invalid audit test data: " + error)));
    }
}

