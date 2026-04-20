package com.acme.orderquestionnaire.domain.bdd;

import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.question.QuestionFactory;
import com.acme.orderquestionnaire.domain.question.answer.AnswerOptionItem;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestionFactory;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionValidationFailure;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireFactory;
import com.acme.orderquestionnaire.domain.questionnaire.tree.QuestionnaireTree;
import com.acme.orderquestionnaire.domain.questionnaire.conditioner.NumericCondition;
import com.acme.orderquestionnaire.testutils.mocks.audit.AuditTestData;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.BddTestSteps;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@BddTestSteps
public class OrderQuestionnaireSteps {

    private QuestionnaireFactory.QuestionnaireBuilder questionnaireBuilder;
    private Questionnaire questionnaire;
    private QuestionnaireTree questionnaireTree;
    private Result<Void, List<QuestionValidationFailure>> validationResult;
    private final Map<String, Object> answers = new HashMap<>();

    @Before
    public void reset() {
        questionnaireBuilder = null;
        questionnaire = null;
        questionnaireTree = null;
        validationResult = null;
        answers.clear();
    }

    @Given("a new questionnaire builder for {string} channel {string} journey {string} and description {string}")
    public void aNewQuestionnaireBuilder(String id, String channel, String journey, String description) {
        questionnaireBuilder = unwrapQuestionnaireBuilder(
                QuestionnaireFactory.createNew(id, channel, journey, description, AuditTestData.createdAudit())
        );
    }

    @And("a number question {string} labeled {string} with sales item {string}")
    public void aNumberQuestion(String id, String label, String salesItemReferenceCode) {
        ensureBuilder();
        Question question = buildQuestion(id, label, salesItemReferenceCode);
        ConfiguredQuestion configuredQuestion = unwrapConfigured(
                unwrapConfiguredBuilder(ConfiguredQuestionFactory.from(question)).asNumber()
        );
        questionnaireBuilder.withQuestion(configuredQuestion);
    }

    @And("an option list question {string} labeled {string} with sales item {string} conditioned on {string} being greater than {int}")
    public void anOptionListQuestionConditionedOnNumericAnswer(String id,
                                                               String label,
                                                               String salesItemReferenceCode,
                                                               String parentQuestionId,
                                                               Integer threshold) {
        ensureBuilder();
        Question question = buildQuestion(id, label, salesItemReferenceCode);
        List<AnswerOptionItem> options = List.of(
                AnswerOptionItem.rehydrate("yes", "Yes"),
                AnswerOptionItem.rehydrate("no", "No")
        );
        ConfiguredQuestion configuredQuestion = unwrapConfigured(
                unwrapConfiguredBuilder(ConfiguredQuestionFactory.from(question))
                        .withCondition(QuestionnaireFactory.condition(
                                new NumericCondition(parentQuestionId, threshold, ">")
                        ).build())
                        .asOptionList(new ConfiguredQuestionFactory.ListConfig(options, "Select one"))
        );
        questionnaireBuilder.withQuestion(configuredQuestion);
    }

    @When("I build the questionnaire from the builder")
    public void iBuildTheQuestionnaireFromTheBuilder() {
        ensureBuilder();
        questionnaire = unwrapQuestionnaire(questionnaireBuilder.build());
        questionnaireTree = questionnaire.toTree();
    }

    @When("I answer question {string} with number {int}")
    public void iAnswerQuestionWithNumber(String questionId, Integer value) {
        answers.put(questionId, value);
    }

    @And("I answer question {string} with text {string}")
    public void iAnswerQuestionWithText(String questionId, String value) {
        answers.put(questionId, value);
    }

    @And("I validate the questionnaire answers")
    public void iValidateTheQuestionnaireAnswers() {
        assertNotNull(questionnaire);
        validationResult = questionnaire.answerValidation(answers);
    }

    @Then("the questionnaire should be created with status {string}")
    public void theQuestionnaireShouldBeCreatedWithStatus(String status) {
        assertNotNull(questionnaire);
        assertEquals(status, questionnaire.status().name());
    }

    @And("the questionnaire should contain {int} ordered questions")
    public void theQuestionnaireShouldContainOrderedQuestions(Integer size) {
        assertNotNull(questionnaire);
        assertEquals(size, questionnaire.getOrderedQuestions().size());
    }

    @And("the questionnaire tree should contain {int} questions")
    public void theQuestionnaireTreeShouldContainQuestions(Integer size) {
        assertNotNull(questionnaireTree);
        assertEquals(size, questionnaireTree.questions().size());
    }

    @Then("the questionnaire validation should succeed")
    public void theQuestionnaireValidationShouldSucceed() {
        assertInstanceOf(Result.Success.class, validationResult);
    }

    @Then("the questionnaire validation should fail with {int} failure for question {string}")
    public void theQuestionnaireValidationShouldFail(Integer failureCount, String questionId) {
        assertInstanceOf(Result.Failure.class, validationResult);
        List<QuestionValidationFailure> failures = validationResult.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));
        assertEquals(failureCount, failures.size());
        assertEquals(questionId, failures.getFirst().questionId());
    }

    private void ensureBuilder() {
        if (questionnaireBuilder == null) {
            throw new IllegalStateException("Questionnaire builder was not initialized");
        }
    }

    private static Question buildQuestion(String id, String label, String salesItemReferenceCode) {
        return unwrapQuestion(
                unwrapQuestionBuilder(
                        QuestionFactory.rehydrate(
                                id,
                                label,
                                ParameterizationStatus.ACTIVE,
                                AuditTestData.createdAudit()
                        )
                )
                .withSalesItemReferenceCode(salesItemReferenceCode)
                .build()
        );
    }

    private static QuestionnaireFactory.QuestionnaireBuilder unwrapQuestionnaireBuilder(
            Result<QuestionnaireFactory.QuestionnaireBuilder, List<DomainError>> result
    ) {
        if (result instanceof Result.Success<QuestionnaireFactory.QuestionnaireBuilder, List<DomainError>>(
                QuestionnaireFactory.QuestionnaireBuilder value)) {
            return value;
        }
        throw new AssertionError("Expected questionnaire builder success");
    }

    private static ConfiguredQuestionFactory.QuestionBuilder unwrapConfiguredBuilder(
            Result<ConfiguredQuestionFactory.QuestionBuilder, List<DomainError>> result
    ) {
        if (result instanceof Result.Success<ConfiguredQuestionFactory.QuestionBuilder, List<DomainError>>(
                ConfiguredQuestionFactory.QuestionBuilder value)) {
            return value;
        }
        throw new AssertionError("Expected configured question builder success");
    }

    private static QuestionFactory.AbstractBuilder<?> unwrapQuestionBuilder(
            Result<? extends QuestionFactory.AbstractBuilder<?>, List<DomainError>> result
    ) {
        if (result instanceof Result.Success<? extends QuestionFactory.AbstractBuilder<?>, List<DomainError>>(
                QuestionFactory.AbstractBuilder<?> value)) {
            return value;
        }
        throw new AssertionError("Expected question builder success");
    }

    private static Question unwrapQuestion(Result<Question, List<DomainError>> result) {
        if (result instanceof Result.Success<Question, List<DomainError>>(Question value)) {
            return value;
        }
        throw new AssertionError("Expected question success");
    }

    private static ConfiguredQuestion unwrapConfigured(Result<ConfiguredQuestion, List<DomainError>> result) {
        if (result instanceof Result.Success<ConfiguredQuestion, List<DomainError>>(ConfiguredQuestion value)) {
            return value;
        }
        throw new AssertionError("Expected configured question success");
    }

    private static Questionnaire unwrapQuestionnaire(Result<Questionnaire, List<DomainError>> result) {
        if (result instanceof Result.Success<Questionnaire, List<DomainError>>(Questionnaire value)) {
            return value;
        }
        throw new AssertionError("Expected questionnaire success");
    }
}

