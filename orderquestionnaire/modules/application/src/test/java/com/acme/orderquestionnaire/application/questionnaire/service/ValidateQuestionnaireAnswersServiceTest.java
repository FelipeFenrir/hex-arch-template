package com.acme.orderquestionnaire.application.questionnaire.service;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.ValidateQuestionnaireAnswersCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.ValidateQuestionnaireAnswersView;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.questionnaire.answer.AnswerOptionItem;
import com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerConfigurationFactory;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.CompositeCondition;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.EqualCondition;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.Id;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@DisplayName("ValidateQuestionnaireAnswersService")
class ValidateQuestionnaireAnswersServiceTest {

    private static final String Q_ID      = "q_survey";
    private static final String CHANNEL   = "APP";
    private static final String JOURNEY   = "JOURNEY_01";

    private QuestionnaireCommandOutPort repository;
    private ValidateQuestionnaireAnswersService service;

    @BeforeEach
    void setUp() {
        repository = mock(QuestionnaireCommandOutPort.class);
        service = new ValidateQuestionnaireAnswersService(repository);
    }

    // ── Constructor guard ─────────────────────────────────────────────────────

    @Test
    @DisplayName("should throw when repository is null")
    void shouldThrowWhenRepositoryIsNull() {
        assertThrows(NullPointerException.class,
                () -> new ValidateQuestionnaireAnswersService(null));
    }

    // ── Command validation failures ───────────────────────────────────────────

    @Test
    @DisplayName("should fail when command is null")
    void shouldFailWhenCommandIsNull() {
        assertFailureWithCode(service.execute(null), "INVALID_COMMAND");
        verify(repository, never()).findQuestionnaireById(any());
    }

    @Test
    @DisplayName("should fail when questionnaireId is blank")
    void shouldFailWhenQuestionnaireIdIsBlank() {
        assertFailureWithCodeAndMessage(service.execute(command("", CHANNEL, JOURNEY, Map.of())),
                "REQUIRED_FIELD", "id");
    }

    @Test
    @DisplayName("should fail when channelDistributionId is blank")
    void shouldFailWhenChannelIsBlank() {
        assertFailureWithCodeAndMessage(service.execute(command(Q_ID, "", JOURNEY, Map.of())),
                "REQUIRED_FIELD", "channelDistributionId");
    }

    @Test
    @DisplayName("should fail when journeyDistributionId is blank")
    void shouldFailWhenJourneyIsBlank() {
        assertFailureWithCodeAndMessage(service.execute(command(Q_ID, CHANNEL, "", Map.of())),
                "REQUIRED_FIELD", "journeyDistributionId");
    }

    @Test
    @DisplayName("should fail when answers map is null")
    void shouldFailWhenAnswersIsNull() {
        assertFailureWithCode(service.execute(command(Q_ID, CHANNEL, JOURNEY, null)), "INVALID_ANSWERS");
    }

    // ── Repository failures ───────────────────────────────────────────────────

    @Test
    @DisplayName("should fail when questionnaire is not found")
    void shouldFailWhenQuestionnaireNotFound() {
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.empty());
        assertFailureWithCode(service.execute(validCommand(Map.of())), "QUESTIONNAIRE_NOT_FOUND");
    }

    // ── Happy paths ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("should return valid=true and empty violations when all answers are correct")
    void shouldReturnValidTrueWhenAllAnswersPass() {
        Questionnaire questionnaire = questionnaireWithNumberQuestion(0.0, 100.0, false, false);
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));

        Result<ValidateQuestionnaireAnswersView, List<DomainError>> result =
                service.execute(validCommand(Map.of("q_income", 50.0)));

        assertInstanceOf(Result.Success.class, result);
        ValidateQuestionnaireAnswersView view = result.getOrElseThrow(e ->
                new IllegalStateException("Expected success: " + e));

        assertTrue(view.valid());
        assertTrue(view.violationsByQuestionId().isEmpty());
        assertEquals(Q_ID, view.questionnaireId());
        assertEquals(CHANNEL, view.channelDistributionId());
        assertEquals(JOURNEY, view.journeyDistributionId());
    }

    @Test
    @DisplayName("should return valid=false with NUMBER violation when number answer violates min constraint")
    void shouldReturnViolationForNumberBelowMin() {
        Questionnaire questionnaire = questionnaireWithNumberQuestion(0.0, 100.0, false, false);
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));

        Result<ValidateQuestionnaireAnswersView, List<DomainError>> result =
                service.execute(validCommand(Map.of("q_income", -10.0)));

        assertInstanceOf(Result.Success.class, result);
        ValidateQuestionnaireAnswersView view = unwrap(result);

        assertFalse(view.valid());
        assertTrue(view.violationsByQuestionId().containsKey("q_income"));

        var questionResult = view.violationsByQuestionId().get("q_income");
        assertFalse(questionResult.violations().isEmpty());
        assertEquals(-10.0, questionResult.providedAnswer());
        assertTrue(questionResult.visibleByCondition());
        assertNotNull(questionResult.answerRule());
        assertEquals("NUMBER", questionResult.answerRule().type());
        assertEquals(0.0, questionResult.answerRule().attributes().get("min"));
        assertEquals(100.0, questionResult.answerRule().attributes().get("max"));
        assertFalse((Boolean) questionResult.answerRule().attributes().get("allowedNegative"));
        assertViolationWithCode(questionResult.violations(), "VALUE_BELOW_MIN", "ANSWER_CONFIGURATION");
    }

    @Test
    @DisplayName("should return valid=false with NUMBER violation when decimal is provided but not allowed")
    void shouldReturnViolationWhenDecimalNotAllowed() {
        Questionnaire questionnaire = questionnaireWithNumberQuestion(null, null, false, true);
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));

        Result<ValidateQuestionnaireAnswersView, List<DomainError>> result =
                service.execute(validCommand(Map.of("q_income", 5.5)));

        ValidateQuestionnaireAnswersView view = unwrap(result);
        assertFalse(view.valid());
        assertViolationWithCode(view.violationsByQuestionId().get("q_income").violations(),
                "DECIMAL_NOT_ALLOWED", "ANSWER_CONFIGURATION");
    }

    @Test
    @DisplayName("should return valid=false with TEXT violation when answer does not match regex")
    void shouldReturnViolationWhenTextDoesNotMatchPattern() {
        Question question = buildQuestion("q_name");
        ConfiguredQuestion cq = ConfiguredQuestion.createNew(
                question, AnswerConfigurationFactory.createTextStrategy("^[A-Z]+$", "Only uppercase letters"), 1);
        Questionnaire questionnaire = questionnaireWith(List.of(cq));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));

        ValidateQuestionnaireAnswersView view = unwrap(
                service.execute(validCommand(Map.of("q_name", "hello"))));

        assertFalse(view.valid());
        var qResult = view.violationsByQuestionId().get("q_name");
        assertNotNull(qResult);
        assertEquals("q_name", qResult.questionId());
        assertEquals("TEXT", qResult.answerRule().type());
        assertEquals("^[A-Z]+$", qResult.answerRule().attributes().get("regexPattern"));
        assertViolationWithCode(qResult.violations(), "PATTERN_MISMATCH", "ANSWER_CONFIGURATION");
    }

    @Test
    @DisplayName("should return valid=false with TEXT violation when answer type is wrong")
    void shouldReturnViolationWhenTextAnswerTypeIsWrong() {
        Question question = buildQuestion("q_name");
        ConfiguredQuestion cq = ConfiguredQuestion.createNew(
                question, AnswerConfigurationFactory.createTextStrategy(), 1);
        Questionnaire questionnaire = questionnaireWith(List.of(cq));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));

        ValidateQuestionnaireAnswersView view = unwrap(
                service.execute(validCommand(Map.of("q_name", 123))));

        assertFalse(view.valid());
        assertViolationWithCode(view.violationsByQuestionId().get("q_name").violations(),
                "INVALID_ANSWER_TYPE", "ANSWER_CONFIGURATION");
    }

    @Test
    @DisplayName("should return valid=false with OPTION_LIST violation when option is invalid")
    void shouldReturnViolationWhenOptionIsInvalid() {
        Question question = buildQuestion("q_status");
        var options = List.of(
                AnswerOptionItem.rehydrate("active", "Active"),
                AnswerOptionItem.rehydrate("inactive", "Inactive"));
        ConfiguredQuestion cq = ConfiguredQuestion.createNew(
                question, AnswerConfigurationFactory.createListStrategy(options), 1);
        Questionnaire questionnaire = questionnaireWith(List.of(cq));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));

        ValidateQuestionnaireAnswersView view = unwrap(
                service.execute(validCommand(Map.of("q_status", "unknown"))));

        assertFalse(view.valid());
        var qResult = view.violationsByQuestionId().get("q_status");
        assertEquals("OPTION_LIST", qResult.answerRule().type());
        assertViolationWithCode(qResult.violations(), "INVALID_OPTION", "ANSWER_CONFIGURATION");
    }

    @Test
    @DisplayName("should return valid=false with DATE violation when past date is not allowed")
    void shouldReturnViolationWhenPastDateNotAllowed() {
        Question question = buildQuestion("q_date");
        ConfiguredQuestion cq = ConfiguredQuestion.createNew(
                question, AnswerConfigurationFactory.createDateStrategy("dd/MM/yyyy", false), 1);
        Questionnaire questionnaire = questionnaireWith(List.of(cq));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));

        ValidateQuestionnaireAnswersView view = unwrap(
                service.execute(validCommand(Map.of("q_date", java.time.LocalDate.of(2000, 1, 1)))));

        assertFalse(view.valid());
        var qResult = view.violationsByQuestionId().get("q_date");
        assertEquals("DATE", qResult.answerRule().type());
        assertFalse((Boolean) qResult.answerRule().attributes().get("allowPastDates"));
        assertViolationWithCode(qResult.violations(), "PAST_DATE_NOT_ALLOWED", "ANSWER_CONFIGURATION");
    }

    @Test
    @DisplayName("should return valid=false with MANDATORY_ANSWER violation when required question has no answer")
    void shouldReturnViolationWhenMandatoryAnswerMissing() {
        Questionnaire questionnaire = questionnaireWithNumberQuestion(null, null, true, true);
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));

        ValidateQuestionnaireAnswersView view = unwrap(service.execute(validCommand(Map.of())));

        assertFalse(view.valid());
        assertViolationWithCode(view.violationsByQuestionId().get("q_income").violations(),
                "MANDATORY_ANSWER", "ANSWER_CONFIGURATION");
    }

    // ── Condition-related violations ──────────────────────────────────────────

    @Test
    @DisplayName("should skip hidden question and return valid=true when hidden question has no answer")
    void shouldSkipHiddenQuestionWithoutAnswer() {
        Question marital = buildQuestion("q_marital");
        Question spouse  = buildQuestion("q_spouse");

        ConfiguredQuestion cqMarital = ConfiguredQuestion.createNew(
                marital, AnswerConfigurationFactory.createTextStrategy(), 1);
        ConfiguredQuestion cqSpouse  = ConfiguredQuestion.createNew(
                spouse, AnswerConfigurationFactory.createTextStrategy(), 2);
        cqSpouse.rootCondition(new EqualCondition("q_marital", "MARRIED"));

        Questionnaire questionnaire = questionnaireWith(List.of(cqMarital, cqSpouse));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));

        // marital = SINGLE → spouse not visible, no answer for spouse → valid
        ValidateQuestionnaireAnswersView view = unwrap(
                service.execute(validCommand(Map.of("q_marital", "SINGLE"))));

        assertTrue(view.valid());
        assertTrue(view.violationsByQuestionId().isEmpty());
    }

    @Test
    @DisplayName("should return ANSWER_NOT_ALLOWED_BY_CONDITION when hidden question receives an answer")
    void shouldReturnConditionViolationWhenHiddenQuestionAnswered() {
        Question marital = buildQuestion("q_marital");
        Question spouse  = buildQuestion("q_spouse");

        ConfiguredQuestion cqMarital = ConfiguredQuestion.createNew(
                marital, AnswerConfigurationFactory.createTextStrategy(), 1);
        ConfiguredQuestion cqSpouse  = ConfiguredQuestion.createNew(
                spouse, AnswerConfigurationFactory.createTextStrategy(), 2);
        cqSpouse.rootCondition(new EqualCondition("q_marital", "MARRIED"));

        Questionnaire questionnaire = questionnaireWith(List.of(cqMarital, cqSpouse));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));

        // marital = SINGLE → spouse not visible, but answer is provided → condition violation
        ValidateQuestionnaireAnswersView view = unwrap(
                service.execute(validCommand(Map.of("q_marital", "SINGLE", "q_spouse", "Ana"))));

        assertFalse(view.valid());

        var spouseResult = view.violationsByQuestionId().get("q_spouse");
        assertNotNull(spouseResult);
        assertFalse(spouseResult.visibleByCondition());
        assertNotNull(spouseResult.conditionRule());
        assertEquals("EQUAL", spouseResult.conditionRule().type());
        assertEquals("q_marital", spouseResult.conditionRule().attributes().get("questionRootCode"));
        assertTrue(spouseResult.dependsOnQuestionIds().contains("q_marital"));
        assertViolationWithCode(spouseResult.violations(), "ANSWER_NOT_ALLOWED_BY_CONDITION", "QUESTION_CONDITION");
    }

    @Test
    @DisplayName("should correctly report composite condition tree with AND operator in conditionRule")
    void shouldReportCompositeConditionTree() {
        Question q1 = buildQuestion("q_employed");
        Question q2 = buildQuestion("q_income");
        Question q3 = buildQuestion("q_pension");

        ConfiguredQuestion cqEmployed = ConfiguredQuestion.createNew(
                q1, AnswerConfigurationFactory.createTextStrategy(), 1);
        ConfiguredQuestion cqIncome = ConfiguredQuestion.createNew(
                q2, AnswerConfigurationFactory.createNumberStrategy(), 2);

        CompositeCondition composite = new CompositeCondition(true); // AND
        composite.addCondition(new EqualCondition("q_employed", "YES"));
        ConfiguredQuestion cqPension = ConfiguredQuestion.createNew(
                q3, AnswerConfigurationFactory.createNumberStrategy(), 3);
        cqPension.rootCondition(composite);

        Questionnaire questionnaire = questionnaireWith(List.of(cqEmployed, cqIncome, cqPension));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));

        // q_employed = NO → pension hidden, but answer provided → condition violation
        ValidateQuestionnaireAnswersView view = unwrap(
                service.execute(validCommand(Map.of("q_employed", "NO", "q_income", 5000.0, "q_pension", 1000.0))));

        assertFalse(view.valid());

        var pensionResult = view.violationsByQuestionId().get("q_pension");
        assertNotNull(pensionResult);
        assertFalse(pensionResult.visibleByCondition());
        assertNotNull(pensionResult.conditionRule());
        assertEquals("COMPOSITE", pensionResult.conditionRule().type());
        assertEquals(1, pensionResult.conditionRule().children().size());
        assertEquals("EQUAL", pensionResult.conditionRule().children().getFirst().type());
        assertTrue(pensionResult.dependsOnQuestionIds().contains("q_employed"));
    }

    @Test
    @DisplayName("should return valid=true when condition is satisfied and visible question answer is correct")
    void shouldReturnValidTrueWhenConditionSatisfiedAndAnswerCorrect() {
        Question marital = buildQuestion("q_marital");
        Question spouse  = buildQuestion("q_spouse");

        ConfiguredQuestion cqMarital = ConfiguredQuestion.createNew(
                marital, AnswerConfigurationFactory.createTextStrategy(), 1);
        ConfiguredQuestion cqSpouse  = ConfiguredQuestion.createNew(
                spouse, AnswerConfigurationFactory.createTextStrategy(), 2);
        cqSpouse.rootCondition(new EqualCondition("q_marital", "MARRIED"));

        Questionnaire questionnaire = questionnaireWith(List.of(cqMarital, cqSpouse));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));

        ValidateQuestionnaireAnswersView view = unwrap(
                service.execute(validCommand(Map.of("q_marital", "MARRIED", "q_spouse", "Ana"))));

        assertTrue(view.valid());
        assertTrue(view.violationsByQuestionId().isEmpty());
    }

    @Test
    @DisplayName("violation view should carry answerRule, conditionRule and dependsOnQuestionIds")
    void violationViewShouldCarryFullContext() {
        Question marital = buildQuestion("q_marital");
        Question spouse  = buildQuestion("q_spouse");

        ConfiguredQuestion cqMarital = ConfiguredQuestion.createNew(
                marital, AnswerConfigurationFactory.createTextStrategy("^[A-Z_]+$", null), 1);
        ConfiguredQuestion cqSpouse  = ConfiguredQuestion.createNew(
                spouse, AnswerConfigurationFactory.createTextStrategy(), 2);
        cqSpouse.rootCondition(new EqualCondition("q_marital", "MARRIED"));

        Questionnaire questionnaire = questionnaireWith(List.of(cqMarital, cqSpouse));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));

        // marital violates regex + spouse is hidden but answered
        ValidateQuestionnaireAnswersView view = unwrap(
                service.execute(validCommand(Map.of("q_marital", "single", "q_spouse", "Ana"))));

        assertFalse(view.valid());

        // marital: answer rule violation
        var maritalResult = view.violationsByQuestionId().get("q_marital");
        assertNotNull(maritalResult);
        assertNotNull(maritalResult.answerRule());
        assertEquals("TEXT", maritalResult.answerRule().type());
        assertNull(maritalResult.conditionRule());
        assertTrue(maritalResult.dependsOnQuestionIds().isEmpty());
        assertTrue(maritalResult.visibleByCondition());

        // spouse: condition violation
        var spouseResult = view.violationsByQuestionId().get("q_spouse");
        assertNotNull(spouseResult);
        assertFalse(spouseResult.visibleByCondition());
        assertNotNull(spouseResult.conditionRule());
        assertNotNull(spouseResult.answerRule());
        assertFalse(spouseResult.dependsOnQuestionIds().isEmpty());
    }

    // ── Inactive question ──────────────────────────────────────────────────────

    @Test
    @DisplayName("should return QUESTION_NOT_ACTIVE violation when inactive question receives an answer")
    void shouldReturnQuestionNotActiveWhenInactiveQuestionAnswered() {
        Question inactiveQ = Question.rehydrate("q_legacy", "Legacy field",
                ParameterizationStatus.DRAFT, "SKU-1", defaultAudit());
        ConfiguredQuestion cq = ConfiguredQuestion.createNew(
                inactiveQ, AnswerConfigurationFactory.createTextStrategy(), 1);
        Questionnaire questionnaire = questionnaireWith(List.of(cq));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));

        ValidateQuestionnaireAnswersView view = unwrap(
                service.execute(validCommand(Map.of("q_legacy", "some_value"))));

        assertFalse(view.valid());
        var qResult = view.violationsByQuestionId().get("q_legacy");
        assertNotNull(qResult);
        assertEquals("some_value", qResult.providedAnswer());
        assertViolationWithCode(qResult.violations(), "QUESTION_NOT_ACTIVE", "QUESTION_STATUS");

        var v = qResult.violations().stream()
                .filter(x -> "QUESTION_NOT_ACTIVE".equals(x.code())).findFirst().orElseThrow();
        assertEquals("QUESTION_STATUS", v.ruleType());
        assertEquals("DRAFT", v.ruleAttributes().get("currentStatus"));
        assertEquals("ACTIVE", v.ruleAttributes().get("expectedStatus"));
        assertEquals("question", v.rulePath());
    }

    @Test
    @DisplayName("should return valid=true when inactive question has no answer")
    void shouldSkipInactiveQuestionWithNoAnswer() {
        Question inactiveQ = Question.rehydrate("q_legacy", "Legacy field",
                ParameterizationStatus.DRAFT, "SKU-1", defaultAudit());
        Question activeQ = buildQuestion("q_name");
        ConfiguredQuestion cqLegacy = ConfiguredQuestion.createNew(
                inactiveQ, AnswerConfigurationFactory.createTextStrategy(), 1);
        ConfiguredQuestion cqName = ConfiguredQuestion.createNew(
                activeQ, AnswerConfigurationFactory.createTextStrategy(), 2);
        Questionnaire questionnaire = questionnaireWith(List.of(cqLegacy, cqName));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));

        ValidateQuestionnaireAnswersView view = unwrap(
                service.execute(validCommand(Map.of("q_name", "Alice"))));

        assertTrue(view.valid());
        assertTrue(view.violationsByQuestionId().isEmpty());
    }

    // ── Condition references inactive question ─────────────────────────────────

    @Test
    @DisplayName("should return CONDITION_REFERENCED_QUESTION_NOT_ACTIVE for simple EQUAL condition referencing inactive question")
    void shouldReturnConditionRefInactiveForSimpleCondition() {
        Question inactiveMarital = Question.rehydrate("q_marital", "Marital status",
                ParameterizationStatus.DRAFT, "SKU-1", defaultAudit());
        Question spouse = buildQuestion("q_spouse");

        ConfiguredQuestion cqMarital = ConfiguredQuestion.createNew(
                inactiveMarital, AnswerConfigurationFactory.createTextStrategy(), 1);
        ConfiguredQuestion cqSpouse = ConfiguredQuestion.createNew(
                spouse, AnswerConfigurationFactory.createTextStrategy(), 2);
        cqSpouse.rootCondition(new com.acme.orderquestionnaire.domain.questionnaire.conditioner.EqualCondition(
                "q_marital", "MARRIED"));

        Questionnaire questionnaire = questionnaireWith(List.of(cqMarital, cqSpouse));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));

        ValidateQuestionnaireAnswersView view = unwrap(
                service.execute(validCommand(Map.of("q_spouse", "Ana"))));

        assertFalse(view.valid());
        var spouseResult = view.violationsByQuestionId().get("q_spouse");
        assertNotNull(spouseResult);
        assertViolationWithCode(spouseResult.violations(), "CONDITION_REFERENCED_QUESTION_NOT_ACTIVE", "QUESTION_CONDITION");

        var v = spouseResult.violations().stream()
                .filter(x -> "CONDITION_REFERENCED_QUESTION_NOT_ACTIVE".equals(x.code())).findFirst().orElseThrow();
        assertEquals("EQUAL_CONDITION", v.ruleType());
        assertEquals("rootCondition", v.rulePath());
        @SuppressWarnings("unchecked")
        List<String> inactiveRefs = (List<String>) v.ruleAttributes().get("inactiveQuestionReferences");
        assertTrue(inactiveRefs.contains("q_marital"));
    }

    @Test
    @DisplayName("should return CONDITION_REFERENCED_QUESTION_NOT_ACTIVE with COMPOSITE_AND ruleType")
    void shouldReturnConditionRefInactiveForCompositeAndCondition() {
        Question inactivePerf = Question.rehydrate("q_perf", "Performance",
                ParameterizationStatus.DRAFT, "SKU-1", defaultAudit());
        Question activeEligible = buildQuestion("q_eligible");
        Question bonus = buildQuestion("q_bonus");

        ConfiguredQuestion cqPerf = ConfiguredQuestion.createNew(
                inactivePerf, AnswerConfigurationFactory.createTextStrategy(), 1);
        ConfiguredQuestion cqEligible = ConfiguredQuestion.createNew(
                activeEligible, AnswerConfigurationFactory.createTextStrategy(), 2);
        ConfiguredQuestion cqBonus = ConfiguredQuestion.createNew(
                bonus, AnswerConfigurationFactory.createNumberStrategy(), 3);

        CompositeCondition composite = new CompositeCondition(true); // AND
        composite.addCondition(new EqualCondition("q_eligible", "YES"));
        composite.addCondition(new EqualCondition("q_perf", "HIGH"));
        cqBonus.rootCondition(composite);

        Questionnaire questionnaire = questionnaireWith(List.of(cqPerf, cqEligible, cqBonus));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));

        ValidateQuestionnaireAnswersView view = unwrap(
                service.execute(validCommand(Map.of("q_eligible", "YES", "q_bonus", 1000.0))));

        assertFalse(view.valid());
        var bonusResult = view.violationsByQuestionId().get("q_bonus");
        assertNotNull(bonusResult);
        assertViolationWithCode(bonusResult.violations(), "CONDITION_REFERENCED_QUESTION_NOT_ACTIVE", "QUESTION_CONDITION");

        var v = bonusResult.violations().stream()
                .filter(x -> "CONDITION_REFERENCED_QUESTION_NOT_ACTIVE".equals(x.code())).findFirst().orElseThrow();
        assertEquals("COMPOSITE_AND", v.ruleType());
        @SuppressWarnings("unchecked")
        List<String> inactiveRefs = (List<String>) v.ruleAttributes().get("inactiveQuestionReferences");
        assertTrue(inactiveRefs.contains("q_perf"));
    }

    @Test
    @DisplayName("should return CONDITION_REFERENCED_QUESTION_NOT_ACTIVE with COMPOSITE_OR ruleType for OR composite")
    void shouldReturnConditionRefInactiveForCompositeOrCondition() {
        Question inactiveMgr = Question.rehydrate("q_mgr", "Manager approval",
                ParameterizationStatus.INACTIVE, "SKU-1", defaultAudit());
        Question inactiveDir = Question.rehydrate("q_dir", "Director approval",
                ParameterizationStatus.DRAFT, "SKU-1", defaultAudit());
        Question approval = buildQuestion("q_approval");

        ConfiguredQuestion cqMgr = ConfiguredQuestion.createNew(
                inactiveMgr, AnswerConfigurationFactory.createTextStrategy(), 1);
        ConfiguredQuestion cqDir = ConfiguredQuestion.createNew(
                inactiveDir, AnswerConfigurationFactory.createTextStrategy(), 2);
        ConfiguredQuestion cqApproval = ConfiguredQuestion.createNew(
                approval, AnswerConfigurationFactory.createTextStrategy(), 3);

        CompositeCondition composite = new CompositeCondition(false); // OR
        composite.addCondition(new EqualCondition("q_mgr", "APPROVED"));
        composite.addCondition(new EqualCondition("q_dir", "APPROVED"));
        cqApproval.rootCondition(composite);

        Questionnaire questionnaire = questionnaireWith(List.of(cqMgr, cqDir, cqApproval));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));

        ValidateQuestionnaireAnswersView view = unwrap(
                service.execute(validCommand(Map.of("q_approval", "YES"))));

        assertFalse(view.valid());
        var approvalResult = view.violationsByQuestionId().get("q_approval");
        assertNotNull(approvalResult);
        assertViolationWithCode(approvalResult.violations(), "CONDITION_REFERENCED_QUESTION_NOT_ACTIVE", "QUESTION_CONDITION");

        var v = approvalResult.violations().stream()
                .filter(x -> "CONDITION_REFERENCED_QUESTION_NOT_ACTIVE".equals(x.code())).findFirst().orElseThrow();
        assertEquals("COMPOSITE_OR", v.ruleType());
        @SuppressWarnings("unchecked")
        List<String> inactiveRefs = (List<String>) v.ruleAttributes().get("inactiveQuestionReferences");
        assertTrue(inactiveRefs.contains("q_mgr"));
        assertTrue(inactiveRefs.contains("q_dir"));
    }

    // ── Violation enrichment for answer config ─────────────────────────────────

    @Test
    @DisplayName("should carry ruleType and ruleAttributes in NUMBER out-of-range violation")
    void shouldCarryRuleTypeAndAttributesInNumberViolation() {
        Questionnaire questionnaire = questionnaireWithNumberQuestion(0.0, 100.0, false, false);
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));

        ValidateQuestionnaireAnswersView view = unwrap(
                service.execute(validCommand(Map.of("q_income", -10.0))));

        var v = view.violationsByQuestionId().get("q_income").violations().stream()
                .filter(x -> "VALUE_BELOW_MIN".equals(x.code())).findFirst().orElseThrow();

        assertEquals("NUMBER_RANGE", v.ruleType());
        assertEquals("answerConfiguration", v.rulePath());
        assertEquals("ANSWER_CONFIGURATION", v.source());
        assertNotNull(v.ruleAttributes());
        assertEquals(0.0, ((Number) v.ruleAttributes().get("min")).doubleValue(), 0.0001);
    }

    @Test
    @DisplayName("should carry ruleType TEXT_PATTERN in PATTERN_MISMATCH violation")
    void shouldCarryTextPatternRuleType() {
        Question q = buildQuestion("q_code");
        ConfiguredQuestion cq = ConfiguredQuestion.createNew(
                q, AnswerConfigurationFactory.createTextStrategy("^[A-Z]+$", "uppercase only"), 1);
        Questionnaire questionnaire = questionnaireWith(List.of(cq));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));

        ValidateQuestionnaireAnswersView view = unwrap(
                service.execute(validCommand(Map.of("q_code", "lowercase"))));

        var v = view.violationsByQuestionId().get("q_code").violations().stream()
                .filter(x -> "PATTERN_MISMATCH".equals(x.code())).findFirst().orElseThrow();
        assertEquals("TEXT_PATTERN", v.ruleType());
        assertEquals("answerConfiguration", v.rulePath());
    }

    @Test
    @DisplayName("should carry ruleType OPTION_LIST_INVALID in INVALID_OPTION violation")
    void shouldCarryOptionListInvalidRuleType() {
        Question q = buildQuestion("q_status");
        var options = List.of(
                AnswerOptionItem.rehydrate("active", "Active"));
        ConfiguredQuestion cq = ConfiguredQuestion.createNew(
                q, AnswerConfigurationFactory.createListStrategy(options), 1);
        Questionnaire questionnaire = questionnaireWith(List.of(cq));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));

        ValidateQuestionnaireAnswersView view = unwrap(
                service.execute(validCommand(Map.of("q_status", "unknown"))));

        var v = view.violationsByQuestionId().get("q_status").violations().stream()
                .filter(x -> "INVALID_OPTION".equals(x.code())).findFirst().orElseThrow();
        assertEquals("OPTION_LIST_INVALID", v.ruleType());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ValidateQuestionnaireAnswersCommand validCommand(Map<String, Object> answers) {
        return command(Q_ID, CHANNEL, JOURNEY, answers);
    }

    private static ValidateQuestionnaireAnswersCommand command(String id, String channel, String journey,
                                                               Map<String, Object> answers) {
        return new ValidateQuestionnaireAnswersCommand(id, channel, journey, answers);
    }

    private static Question buildQuestion(String id) {
        return Question.rehydrate(id, "Label for " + id, ParameterizationStatus.ACTIVE, "SKU-1", defaultAudit());
    }

    private static Questionnaire questionnaireWithNumberQuestion(Double min, Double max,
                                                                  boolean allowedDecimal, boolean allowedNegative) {
        Question question = buildQuestion("q_income");
        ConfiguredQuestion cq = ConfiguredQuestion.createNew(
                question,
                AnswerConfigurationFactory.createNumberStrategy(min, max, null, allowedDecimal, allowedNegative, null),
                1);
        return questionnaireWith(List.of(cq));
    }

    private static Questionnaire questionnaireWith(List<ConfiguredQuestion> configuredQuestions) {
        return Questionnaire.rehydrate(
                QuestionnaireId.of(Q_ID, CHANNEL, JOURNEY),
                "Survey questionnaire",
                ParameterizationStatus.ACTIVE,
                configuredQuestions,
                defaultAudit());
    }

    private static AuditInfo defaultAudit() {
        return OrderQuestionnaireAuditFactory.createNew(
                Id.withId("00000000-0000-0000-0000-000000000001"),
                "REF-1", "Test User", "test@acme.com",
                LocalDateTime.parse("2026-01-01T10:00:00"))
                .getOrElseThrow(e -> new IllegalStateException("Invalid audit: " + e));
    }

    private static void assertFailureWithCode(Result<ValidateQuestionnaireAnswersView, List<DomainError>> result,
                                              String code) {
        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals(code)),
                "Expected error code '%s' but got: %s".formatted(code, errors));
    }

    private static void assertFailureWithCodeAndMessage(
            Result<ValidateQuestionnaireAnswersView, List<DomainError>> result,
            String code,
            String messageFragment) {
        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));
        assertTrue(errors.stream()
                        .anyMatch(e -> e.code().equals(code) && e.message().contains(messageFragment)),
                "Expected error code '%s' containing '%s' but got: %s"
                        .formatted(code, messageFragment, errors));
    }

    private static ValidateQuestionnaireAnswersView unwrap(
            Result<ValidateQuestionnaireAnswersView, List<DomainError>> result) {
        return result.getOrElseThrow(e -> new IllegalStateException("Expected success but got failure: " + e));
    }

    private static void assertViolationWithCode(
            List<com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionAnswerViolationView> violations,
            String code, String source) {
        assertTrue(violations.stream().anyMatch(v -> v.code().equals(code)),
                "Expected violation code '%s' but got: %s".formatted(code, violations));
        assertTrue(violations.stream().anyMatch(v -> v.source().equals(source)),
                "Expected source '%s' but got: %s".formatted(source, violations));
    }
}




