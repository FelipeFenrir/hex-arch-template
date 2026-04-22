package com.acme.orderquestionnaire.application.questionnaire.bdd;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.AnswerConfigParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.ConfiguredQuestionParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateConfiguredQuestionParam;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.UpdateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireUpdatedView;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.UpdateQuestionnaireService;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.BddTestSteps;
import com.acme.shared.vo.Id;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@BddTestSteps
public class UpdateQuestionnaireStepDefs {

    private static final String CHANNEL = "APP";
    private static final String JOURNEY = "JOURNEY_01";

    private QuestionnaireCommandOutPort questionnaireRepository;
    private QuestionCommandOutPort questionRepository;
    private UpdateQuestionnaireService service;

    private UpdateQuestionnaireCommand command;
    private Result<QuestionnaireUpdatedView, List<DomainError>> result;
    private QuestionnaireUpdatedView successView;

    @Before
    public void setup() {
        questionnaireRepository = mock(QuestionnaireCommandOutPort.class);
        questionRepository = mock(QuestionCommandOutPort.class);
        service = new UpdateQuestionnaireService(questionnaireRepository, questionRepository);
        command = null;
        result = null;
        successView = null;
        when(questionnaireRepository.update(any())).thenAnswer(inv -> Result.success(inv.getArgument(0)));
    }

    @Given("an existing questionnaire {string} with status {string}")
    public void anExistingQuestionnaireWithStatus(String questionnaireId, String status) {
        ParameterizationStatus parsedStatus = ParameterizationStatus.valueOf(status);
        Questionnaire questionnaire = Questionnaire.rehydrate(
                QuestionnaireId.of(questionnaireId, CHANNEL, JOURNEY),
                "Current description",
                parsedStatus,
                List.of(),
                OrderQuestionnaireAuditFactory.createNew(
                        Id.withId("11111111-1111-1111-1111-111111111111"),
                        "REF",
                        "Creator",
                        "creator@acme.com",
                        LocalDateTime.parse("2026-01-01T10:00:00"))
                        .getOrElseThrow(error -> new IllegalStateException("Invalid audit test data: " + error)));
        when(questionnaireRepository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));
    }

    @Given("an existing questionnaire {string} with status {string} already containing question {string}")
    public void anExistingQuestionnaireWithStatusAlreadyContainingQuestion(String questionnaireId,
                                                                           String status,
                                                                           String existingQuestionId) {
        ParameterizationStatus parsedStatus = ParameterizationStatus.valueOf(status);
        Question existingQuestion = Question.rehydrate(
                existingQuestionId,
                "Label " + existingQuestionId,
                ParameterizationStatus.ACTIVE,
                "SKU-1",
                OrderQuestionnaireAuditFactory.createNew(
                                Id.withId("11111111-1111-1111-1111-111111111111"),
                                "REF",
                                "Creator",
                                "creator@acme.com",
                                LocalDateTime.parse("2026-01-01T10:00:00"))
                        .getOrElseThrow(error -> new IllegalStateException("Invalid audit test data: " + error))
        );
        var configuredExistingQuestion = com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion.createNew(
                existingQuestion,
                com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerConfigurationFactory.createNumberStrategy(),
                0
        );
        Questionnaire questionnaire = Questionnaire.rehydrate(
                QuestionnaireId.of(questionnaireId, CHANNEL, JOURNEY),
                "Current description",
                parsedStatus,
                List.of(configuredExistingQuestion),
                OrderQuestionnaireAuditFactory.createNew(
                        Id.withId("11111111-1111-1111-1111-111111111111"),
                        "REF",
                        "Creator",
                        "creator@acme.com",
                        LocalDateTime.parse("2026-01-01T10:00:00"))
                        .getOrElseThrow(error -> new IllegalStateException("Invalid audit test data: " + error)));
        when(questionnaireRepository.findQuestionnaireById(any())).thenReturn(Optional.of(questionnaire));
    }

    @And("question {string} exists for update")
    public void questionExistsForUpdate(String questionId) {
        Question question = Question.rehydrate(
                questionId,
                "Label " + questionId,
                ParameterizationStatus.ACTIVE,
                "SKU-1",
                OrderQuestionnaireAuditFactory.createNew(
                        Id.withId("11111111-1111-1111-1111-111111111111"),
                        "REF",
                        "Creator",
                        "creator@acme.com",
                        LocalDateTime.parse("2026-01-01T10:00:00"))
                        .getOrElseThrow(error -> new IllegalStateException("Invalid audit test data: " + error)));
        when(questionRepository.findQuestionById(questionId)).thenReturn(Optional.of(question));
    }

    @And("an update questionnaire command adding question {string} and status {string}")
    public void anUpdateQuestionnaireCommandAddingQuestionAndStatus(String questionId, String status) {
        UpdateConfiguredQuestionParam upsert = new UpdateConfiguredQuestionParam(
                questionId,
                new ConfiguredQuestionParam(1, new AnswerConfigParam.Number(null, null, null, true, true, null), null));

        command = new UpdateQuestionnaireCommand(
                "q_001",
                CHANNEL,
                JOURNEY,
                "Updated description",
                ParameterizationStatus.valueOf(status),
                List.of(upsert),
                List.of(),
                new AuditUserParam(
                        Id.withId("22222222-2222-2222-2222-222222222222"),
                        "REF-UPD",
                        "Updater",
                        "updater@acme.com"),
                LocalDateTime.parse("2026-01-01T11:00:00"));
    }

    @And("an update questionnaire command with status {string} and no configured questions")
    public void anUpdateQuestionnaireCommandWithStatusAndNoConfiguredQuestions(String status) {
        command = new UpdateQuestionnaireCommand(
                "q_001",
                CHANNEL,
                JOURNEY,
                "Updated description",
                ParameterizationStatus.valueOf(status),
                List.of(),
                List.of(),
                new AuditUserParam(
                        Id.withId("22222222-2222-2222-2222-222222222222"),
                        "REF-UPD",
                        "Updater",
                        "updater@acme.com"),
                LocalDateTime.parse("2026-01-01T11:00:00"));
    }

    @And("an update questionnaire command adding question {string} with condition on {string}")
    public void anUpdateQuestionnaireCommandAddingQuestionWithConditionOn(String questionId, String conditionQuestionId) {
        UpdateConfiguredQuestionParam upsert = new UpdateConfiguredQuestionParam(
                questionId,
                new ConfiguredQuestionParam(
                        1,
                        new AnswerConfigParam.Number(null, null, null, true, true, null),
                        new com.acme.orderquestionnaire.application.questionnaire.dto.command.ConditionParam.Equal(conditionQuestionId, "yes")
                )
        );

        command = new UpdateQuestionnaireCommand(
                "q_001",
                CHANNEL,
                JOURNEY,
                "Updated description",
                ParameterizationStatus.DRAFT,
                List.of(upsert),
                List.of(),
                new AuditUserParam(
                        Id.withId("22222222-2222-2222-2222-222222222222"),
                        "REF-UPD",
                        "Updater",
                        "updater@acme.com"),
                LocalDateTime.parse("2026-01-01T11:00:00"));
    }

    @When("I execute the update questionnaire use case")
    public void iExecuteTheUpdateQuestionnaireUseCase() {
        result = service.execute(command);
    }

    @Then("the update questionnaire result should be a success")
    public void theUpdateQuestionnaireResultShouldBeASuccess() {
        assertInstanceOf(Result.Success.class, result);
        successView = result.getOrElseThrow(error ->
                new IllegalStateException("Expected success but got failure: " + error));
    }

    @And("the update view should contain configured question {string}")
    public void theUpdateViewShouldContainConfiguredQuestion(String questionId) {
        assertNotNull(successView);
        assertTrue(successView.configuredQuestions().stream().anyMatch(question -> question.questionId().equals(questionId)));
    }

    @And("the configured question {string} should have answer type {string}")
    public void theConfiguredQuestionShouldHaveAnswerType(String questionId, String answerType) {
        assertNotNull(successView);
        var configuredQuestion = successView.configuredQuestions().stream()
                .filter(question -> question.questionId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Configured question not found in view"));

        assertNotNull(configuredQuestion.answerConfiguration());
        assertEquals(answerType, configuredQuestion.answerConfiguration().type());
    }

    @And("the configured question {string} should have no dependencies")
    public void theConfiguredQuestionShouldHaveNoDependencies(String questionId) {
        assertNotNull(successView);
        var configuredQuestion = successView.configuredQuestions().stream()
                .filter(question -> question.questionId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Configured question not found in view"));

        assertTrue(configuredQuestion.dependsOnQuestionIds().isEmpty());
    }

    @And("the configured question {string} should depend on question {string}")
    public void theConfiguredQuestionShouldDependOnQuestion(String questionId, String dependencyQuestionId) {
        assertNotNull(successView);
        var configuredQuestion = successView.configuredQuestions().stream()
                .filter(question -> question.questionId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Configured question not found in view"));

        assertNotNull(configuredQuestion.rootCondition());
        assertEquals("EQUAL", configuredQuestion.rootCondition().type());
        assertTrue(configuredQuestion.dependsOnQuestionIds().contains(dependencyQuestionId));
    }

    @And("the questionnaire update repository call should have happened")
    public void theQuestionnaireUpdateRepositoryCallShouldHaveHappened() {
        verify(questionnaireRepository).update(any(Questionnaire.class));
    }

    @Then("the update questionnaire result should fail with error code {string}")
    public void theUpdateQuestionnaireResultShouldFailWithErrorCode(String expectedCode) {
        assertInstanceOf(Result.Failure.class, result);
        Result.Failure<QuestionnaireUpdatedView, List<DomainError>> failure =
                (Result.Failure<QuestionnaireUpdatedView, List<DomainError>>) result;
        assertTrue(failure.error().stream().anyMatch(error -> error.code().equals(expectedCode)),
                "Expected code %s but got %s".formatted(expectedCode, failure.error()));
    }
}

