package com.acme.orderquestionnaire.application.questionnaire.bdd;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.ValidateQuestionnaireAnswersCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionAnswerValidationView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.ValidateQuestionnaireAnswersView;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.ValidateQuestionnaireAnswersService;
import com.acme.orderquestionnaire.application.questionnaire.service.context.ValidateQuestionnaireAnswersPipelineContext;
import com.acme.orderquestionnaire.application.questionnaire.service.step.FetchQuestionnaireForAnswersValidationStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.ValidateAnswersAgainstQuestionnaireStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.ValidateQuestionnaireAnswersCommandStep;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.questionnaire.answer.AnswerOptionItem;
import com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerConfigurationFactory;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.EqualCondition;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.pipeline.Step;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.BddTestSteps;
import com.acme.shared.vo.AuditInfo;
import com.acme.shared.vo.Id;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@BddTestSteps
public class ValidateQuestionnaireAnswersStepDefs {

    private QuestionnaireCommandOutPort repository;
    private ValidateQuestionnaireAnswersService service;

    private Questionnaire currentQuestionnaire;
    private final Map<String, Object> pendingAnswers = new HashMap<>();

    private ValidateQuestionnaireAnswersCommand command;
    private Result<ValidateQuestionnaireAnswersView, List<DomainError>> result;

    @Before
    public void setUpValidation() {
        repository = mock(QuestionnaireCommandOutPort.class);
        service = buildService(repository);
        currentQuestionnaire = null;
        pendingAnswers.clear();
        command = null;
        result = null;
    }

    // ── Given ─────────────────────────────────────────────────────────────────

    @Given("a questionnaire {string} with channel {string} and journey {string} has a number question {string} with min {double} max {double} no decimals no negatives")
    public void givenQuestionnaireWithNumberQuestion(String qId, String channel, String journey,
                                                     String questionId, double min, double max) {
        ConfiguredQuestion cq = ConfiguredQuestion.createNew(
                buildQuestion(questionId),
                AnswerConfigurationFactory.createNumberStrategy(min, max, null, false, false, null),
                1);
        currentQuestionnaire = buildQuestionnaire(qId, channel, journey, List.of(cq));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(currentQuestionnaire));
    }

    @Given("a questionnaire {string} with channel {string} and journey {string} has a text question {string} with regex {string}")
    public void givenQuestionnaireWithTextQuestion(String qId, String channel, String journey,
                                                   String questionId, String regex) {
        ConfiguredQuestion cq = ConfiguredQuestion.createNew(
                buildQuestion(questionId),
                AnswerConfigurationFactory.createTextStrategy(regex, "Pattern not matched"),
                1);
        currentQuestionnaire = buildQuestionnaire(qId, channel, journey, List.of(cq));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(currentQuestionnaire));
    }

    @Given("a questionnaire {string} with channel {string} and journey {string} has an option question {string} with options {string} and {string}")
    public void givenQuestionnaireWithOptionQuestion(String qId, String channel, String journey,
                                                     String questionId, String opt1, String opt2) {
        List<AnswerOptionItem> options = List.of(
                AnswerOptionItem.rehydrate(opt1.toLowerCase(), opt1),
                AnswerOptionItem.rehydrate(opt2.toLowerCase(), opt2));
        ConfiguredQuestion cq = ConfiguredQuestion.createNew(
                buildQuestion(questionId),
                AnswerConfigurationFactory.createListStrategy(options),
                1);
        currentQuestionnaire = buildQuestionnaire(qId, channel, journey, List.of(cq));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(currentQuestionnaire));
    }

    @Given("a questionnaire {string} with channel {string} and journey {string} has question {string} and conditional question {string} visible when {string} equals {string}")
    public void givenQuestionnaireWithConditionalQuestion(String qId, String channel, String journey,
                                                          String rootId, String condId,
                                                          String triggerQ, String triggerVal) {
        ConfiguredQuestion cqRoot = ConfiguredQuestion.createNew(
                buildQuestion(rootId), AnswerConfigurationFactory.createTextStrategy(), 1);
        ConfiguredQuestion cqCond = ConfiguredQuestion.createNew(
                buildQuestion(condId), AnswerConfigurationFactory.createTextStrategy(), 2);
        cqCond.rootCondition(new EqualCondition(triggerQ, triggerVal));
        currentQuestionnaire = buildQuestionnaire(qId, channel, journey, List.of(cqRoot, cqCond));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(currentQuestionnaire));
    }

    @Given("questionnaire {string} with channel {string} and journey {string} does not exist for validation")
    public void givenQuestionnaireDoesNotExistForValidation(String qId, String channel, String journey) {
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.empty());
    }

    @Given("a null validate answers command")
    public void givenNullValidateAnswersCommand() {
        command = null;
    }

    @And("the numeric answer for {string} is {double}")
    public void theNumericAnswerForIs(String questionId, double value) {
        pendingAnswers.put(questionId, value);
    }

    @And("the text answer for {string} is {string}")
    public void theTextAnswerForIs(String questionId, String value) {
        pendingAnswers.put(questionId, value);
    }

    @Given("a questionnaire {string} with channel {string} and journey {string} has an inactive number question {string} with min {double} max {double}")
    public void givenQuestionnaireWithInactiveNumberQuestion(String qId, String channel, String journey,
                                                              String questionId, double min, double max) {
        Question inactiveQuestion = Question.rehydrate(
                questionId,
                "Label for " + questionId,
                ParameterizationStatus.DRAFT,
                "SKU-1",
                defaultAudit()
        );

        ConfiguredQuestion cq = ConfiguredQuestion.createNew(
                inactiveQuestion,
                AnswerConfigurationFactory.createNumberStrategy(min, max, null, false, false, null),
                1
        );

        currentQuestionnaire = buildQuestionnaire(qId, channel, journey, List.of(cq));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(currentQuestionnaire));
    }

    @Given("a questionnaire {string} with channel {string} and journey {string} has question {string} and conditional question {string} conditioned on inactive question {string}")
    public void givenQuestionnaireWithConditionedOnInactiveQuestion(String qId, String channel, String journey,
                                                                     String rootId, String dependentId,
                                                                     String inactiveRootId) {
        Question inactiveRoot = Question.rehydrate(
                rootId,
                "Label for " + rootId,
                ParameterizationStatus.DRAFT,
                "SKU-1",
                defaultAudit()
        );

        ConfiguredQuestion cqRoot = ConfiguredQuestion.createNew(
                inactiveRoot,
                AnswerConfigurationFactory.createTextStrategy(),
                1
        );

        ConfiguredQuestion cqDep = ConfiguredQuestion.createNew(
                buildQuestion(dependentId),
                AnswerConfigurationFactory.createTextStrategy(),
                2
        );
        cqDep.rootCondition(new EqualCondition(inactiveRootId, "YES"));

        currentQuestionnaire = buildQuestionnaire(qId, channel, journey, List.of(cqRoot, cqDep));
        when(repository.findQuestionnaireById(any())).thenReturn(Optional.of(currentQuestionnaire));
    }

    // ── When ──────────────────────────────────────────────────────────────────

    @When("I submit the answers for validation")
    public void iSubmitTheAnswersForValidation() {
        command = new ValidateQuestionnaireAnswersCommand(
                currentQuestionnaire.id(),
                currentQuestionnaire.channelDistributionId(),
                currentQuestionnaire.journeyDistributionId(),
                new HashMap<>(pendingAnswers));
        result = service.execute(command);
    }

    @When("I submit the answers for questionnaire {string} channel {string} journey {string}")
    public void iSubmitAnswersForQuestionnaire(String qId, String channel, String journey) {
        command = new ValidateQuestionnaireAnswersCommand(qId, channel, journey, new HashMap<>(pendingAnswers));
        result = service.execute(command);
    }

    @When("I submit null answers for questionnaire {string} channel {string} journey {string}")
    public void iSubmitNullAnswersForQuestionnaire(String qId, String channel, String journey) {
        command = new ValidateQuestionnaireAnswersCommand(qId, channel, journey, null);
        result = service.execute(command);
    }

    @When("I execute the validate answers use case with null command")
    public void iExecuteWithNullCommand() {
        result = service.execute(null);
    }

    // ── Then ──────────────────────────────────────────────────────────────────

    @Then("the validation result should be a success")
    public void theValidationResultShouldBeSuccess() {
        assertInstanceOf(Result.Success.class, result,
                "Expected success but got: " + (result instanceof Result.Failure<?, ?> f ? f.error() : result));
    }

    @Then("the validation result should fail with error code {string}")
    public void theValidationResultShouldFail(String expectedCode) {
        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure"));
        assertTrue(errors.stream().anyMatch(e -> e.code().equals(expectedCode)),
                "Expected error code '%s' but got: %s".formatted(expectedCode, errors));
    }

    @Then("the result should be valid")
    public void theResultShouldBeValid() {
        assertTrue(unwrap().valid(), "Expected valid=true but got violations: " + unwrap().violationsByQuestionId());
    }

    @Then("the result should be invalid")
    public void theResultShouldBeInvalid() {
        assertFalse(unwrap().valid(), "Expected valid=false but was valid");
    }

    @And("question {string} should have violation with code {string} and source {string}")
    public void questionShouldHaveViolation(String questionId, String code, String source) {
        QuestionAnswerValidationView qView = unwrap().violationsByQuestionId().get(questionId);
        assertNotNull(qView, "No validation result for question: " + questionId);
        assertTrue(qView.violations().stream().anyMatch(v -> v.code().equals(code)),
                "Expected code '%s' in %s".formatted(code, qView.violations()));
        assertTrue(qView.violations().stream().anyMatch(v -> v.source().equals(source)),
                "Expected source '%s' in %s".formatted(source, qView.violations()));
    }

    @And("question {string} answer rule type should be {string}")
    public void questionAnswerRuleType(String questionId, String expectedType) {
        QuestionAnswerValidationView qView = unwrap().violationsByQuestionId().get(questionId);
        assertNotNull(qView);
        assertNotNull(qView.answerRule(), "answerRule is null for question: " + questionId);
        assertEquals(expectedType, qView.answerRule().type());
    }

    @And("question {string} answer rule should contain attribute {string} equal to {double}")
    public void questionAnswerRuleAttribute(String questionId, String attribute, double expected) {
        QuestionAnswerValidationView qView = unwrap().violationsByQuestionId().get(questionId);
        assertNotNull(qView);
        Object actual = qView.answerRule().attributes().get(attribute);
        assertNotNull(actual, "Attribute '%s' not found".formatted(attribute));
        assertEquals(expected, ((Number) actual).doubleValue(), 0.0001);
    }

    @And("question {string} should not be visible by condition")
    public void questionShouldNotBeVisibleByCondition(String questionId) {
        assertFalse(unwrap().violationsByQuestionId().get(questionId).visibleByCondition());
    }

    @And("question {string} condition rule type should be {string}")
    public void questionConditionRuleType(String questionId, String expectedType) {
        QuestionAnswerValidationView qView = unwrap().violationsByQuestionId().get(questionId);
        assertNotNull(qView.conditionRule(), "conditionRule is null for question: " + questionId);
        assertEquals(expectedType, qView.conditionRule().type());
    }

    @And("question {string} depends on question {string}")
    public void questionDependsOn(String questionId, String dependsOn) {
        QuestionAnswerValidationView qView = unwrap().violationsByQuestionId().get(questionId);
        assertTrue(qView.dependsOnQuestionIds().contains(dependsOn),
                "Expected dependsOn '%s' but got: %s".formatted(dependsOn, qView.dependsOnQuestionIds()));
    }

    @And("question {string} violation ruleType should be {string}")
    public void questionViolationRuleTypeShouldBe(String questionId, String expectedRuleType) {
        QuestionAnswerValidationView qView = unwrap().violationsByQuestionId().get(questionId);
        assertNotNull(qView, "No validation result for question: " + questionId);
        assertTrue(qView.violations().stream().anyMatch(v -> expectedRuleType.equals(v.ruleType())),
                "Expected ruleType '%s' in %s".formatted(expectedRuleType, qView.violations()));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ValidateQuestionnaireAnswersView unwrap() {
        return result.getOrElseThrow(e -> new IllegalStateException("Expected success but got failure: " + e));
    }

    private static Question buildQuestion(String id) {
        return Question.rehydrate(id, "Label for " + id, ParameterizationStatus.ACTIVE, "SKU-1", defaultAudit());
    }

    private static Questionnaire buildQuestionnaire(String id, String channel, String journey,
                                                    List<ConfiguredQuestion> questions) {
        return Questionnaire.rehydrate(
                QuestionnaireId.of(id, channel, journey),
                "Survey questionnaire",
                ParameterizationStatus.ACTIVE,
                new ArrayList<>(questions),
                defaultAudit());
    }

    private static AuditInfo defaultAudit() {
        return OrderQuestionnaireAuditFactory.createNew(
                Id.withId("00000000-0000-0000-0000-000000000001"),
                "REF-1", "Test User", "test@acme.com",
                LocalDateTime.parse("2026-01-01T10:00:00"))
                .getOrElseThrow(e -> new IllegalStateException("Invalid audit: " + e));
    }

    private static ValidateQuestionnaireAnswersService buildService(QuestionnaireCommandOutPort repo) {
        List<Step<ValidateQuestionnaireAnswersPipelineContext>> steps = List.of(
                new ValidateQuestionnaireAnswersCommandStep(),
                new FetchQuestionnaireForAnswersValidationStep(repo),
                new ValidateAnswersAgainstQuestionnaireStep()
        );
        return new ValidateQuestionnaireAnswersService(steps);
    }
}

