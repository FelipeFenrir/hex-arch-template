package com.acme.orderquestionnaire.application.bdd;

import com.acme.orderquestionnaire.application.questionnaire.dto.command.DeleteQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.DeleteQuestionnairesResultView;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.DeleteQuestionnaireService;
import com.acme.orderquestionnaire.application.questionnaire.service.context.DeleteQuestionnairePipelineContext;
import com.acme.orderquestionnaire.application.questionnaire.service.step.DeleteQuestionnaireStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.FetchQuestionnaireForDeleteStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.ValidateDeleteQuestionnaireCommandStep;
import com.acme.orderquestionnaire.application.questionnaire.service.step.ValidateQuestionnaireDeleteEligibilityStep;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.questionnaire.answer.strategy.AnswerConfigurationFactory;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.pattern.pipeline.Step;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@BddTestSteps
public class DeleteQuestionnaireStepDefs {

    private QuestionnaireCommandOutPort questionnaireRepository;
    private DeleteQuestionnaireService service;

    private final Map<QuestionnaireId, Questionnaire> questionnaires = new HashMap<>();

    private DeleteQuestionnaireCommand singleCommand;
    private List<DeleteQuestionnaireCommand> batchCommands;
    private Result<Void, List<DomainError>> singleResult;
    private Result<DeleteQuestionnairesResultView, List<DomainError>> batchResult;

    @Before
    public void setUp() {
        questionnaireRepository = mock(QuestionnaireCommandOutPort.class);
        service = buildService(questionnaireRepository);

        questionnaires.clear();
        singleCommand = null;
        batchCommands = null;
        singleResult = null;
        batchResult = null;

        when(questionnaireRepository.findQuestionnaireById(any()))
                .thenAnswer(invocation -> Optional.ofNullable(questionnaires.get(invocation.getArgument(0))));
        when(questionnaireRepository.deleteById(any())).thenReturn(Result.success(null));
    }

    @Given("questionnaire {string} with channel {string} and journey {string} exists for delete with status {string}")
    public void questionnaireExistsForDeleteWithStatus(String id, String channel, String journey, String status) {
        DeleteQuestionnaireCommand command = new DeleteQuestionnaireCommand(id, channel, journey);
        questionnaires.put(
                QuestionnaireId.of(id, channel, journey),
                questionnaire(command, ParameterizationStatus.valueOf(status), false)
        );
    }

    @Given("questionnaire {string} with channel {string} and journey {string} exists for delete with status {string} and configured questions")
    public void questionnaireExistsForDeleteWithStatusAndConfiguredQuestions(String id,
                                                                             String channel,
                                                                             String journey,
                                                                             String status) {
        DeleteQuestionnaireCommand command = new DeleteQuestionnaireCommand(id, channel, journey);
        questionnaires.put(
                QuestionnaireId.of(id, channel, journey),
                questionnaire(command, ParameterizationStatus.valueOf(status), true)
        );
    }

    @Given("questionnaire {string} with channel {string} and journey {string} does not exist for delete")
    public void questionnaireDoesNotExistForDelete(String id, String channel, String journey) {
        questionnaires.remove(QuestionnaireId.of(id, channel, journey));
    }

    @And("a delete questionnaire request for id {string}, channel {string}, journey {string}")
    public void aDeleteQuestionnaireRequest(String id, String channel, String journey) {
        singleCommand = new DeleteQuestionnaireCommand(id, channel, journey);
    }

    @And("a delete questionnaires batch request with commands {string}")
    public void aDeleteQuestionnairesBatchRequest(String rawCommands) {
        batchCommands = rawCommands.isBlank()
                ? List.of()
                : List.of(rawCommands.split(";")).stream()
                .map(this::toCommand)
                .toList();
    }

    @Given("a null delete questionnaires batch request")
    public void aNullDeleteQuestionnairesBatchRequest() {
        batchCommands = null;
    }

    @When("I execute delete questionnaire")
    public void iExecuteDeleteQuestionnaire() {
        singleResult = service.execute(singleCommand);
    }

    @When("I execute delete questionnaires batch")
    public void iExecuteDeleteQuestionnairesBatch() {
        batchResult = service.execute(batchCommands);
    }

    @Then("delete questionnaire should succeed")
    public void deleteQuestionnaireShouldSucceed() {
        assertInstanceOf(Result.Success.class, singleResult);
        verify(questionnaireRepository).deleteById(any(QuestionnaireId.class));
    }

    @Then("delete questionnaire should fail with code {string}")
    public void deleteQuestionnaireShouldFailWithCode(String code) {
        assertInstanceOf(Result.Failure.class, singleResult);
        Result.Failure<Void, List<DomainError>> failure = (Result.Failure<Void, List<DomainError>>) singleResult;
        assertTrue(failure.error().stream().anyMatch(error -> error.code().equals(code)));
    }

    @And("delete questionnaire should fail with message {string}")
    public void deleteQuestionnaireShouldFailWithMessage(String expectedMessage) {
        assertInstanceOf(Result.Failure.class, singleResult);
        Result.Failure<Void, List<DomainError>> failure = (Result.Failure<Void, List<DomainError>>) singleResult;
        DomainError firstError = failure.error().getFirst();
        assertEquals(expectedMessage, firstError.message());
    }

    @Then("delete questionnaires batch should succeed with {int} failures")
    public void deleteQuestionnairesBatchShouldSucceedWithFailures(int failuresCount) {
        assertInstanceOf(Result.Success.class, batchResult);
        DeleteQuestionnairesResultView view = batchResult.getOrElseThrow(errors ->
                new IllegalStateException("Expected success but got failure: " + errors));
        assertEquals(failuresCount, view.failures().size());
    }

    @And("delete questionnaires batch should contain questionnaire {string}, channel {string}, journey {string} with code {string}")
    public void deleteQuestionnairesBatchShouldContainQuestionnaireWithCode(String id,
                                                                            String channel,
                                                                            String journey,
                                                                            String code) {
        DeleteQuestionnairesResultView view = batchResult.getOrElseThrow(errors ->
                new IllegalStateException("Expected success but got failure: " + errors));
        assertTrue(view.failures().stream().anyMatch(failure ->
                failure.id().equals(id)
                        && failure.channelDistributionId().equals(channel)
                        && failure.journeyDistributionId().equals(journey)
                        && failure.code().equals(code)
        ));
    }

    @Then("delete questionnaires batch should fail globally with code {string}")
    public void deleteQuestionnairesBatchShouldFailGloballyWithCode(String code) {
        assertInstanceOf(Result.Failure.class, batchResult);
        Result.Failure<DeleteQuestionnairesResultView, List<DomainError>> failure =
                (Result.Failure<DeleteQuestionnairesResultView, List<DomainError>>) batchResult;
        assertTrue(failure.error().stream().anyMatch(error -> error.code().equals(code)));
    }

    private static DeleteQuestionnaireService buildService(QuestionnaireCommandOutPort repo) {
        List<Step<DeleteQuestionnairePipelineContext>> steps = List.of(
                new ValidateDeleteQuestionnaireCommandStep(),
                new FetchQuestionnaireForDeleteStep(repo),
                new ValidateQuestionnaireDeleteEligibilityStep(),
                new DeleteQuestionnaireStep(repo)
        );
        return new DeleteQuestionnaireService(steps);
    }

    private DeleteQuestionnaireCommand toCommand(String raw) {
        String[] parts = raw.split("\\|");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid command format: " + raw);
        }
        return new DeleteQuestionnaireCommand(parts[0], parts[1], parts[2]);
    }

    private static Questionnaire questionnaire(DeleteQuestionnaireCommand command,
                                               ParameterizationStatus status,
                                               boolean includeConfiguredQuestions) {
        List<ConfiguredQuestion> configuredQuestions = includeConfiguredQuestions
                ? List.of(ConfiguredQuestion.createNew(
                        Question.rehydrate(
                                "q_001",
                                "Question",
                                ParameterizationStatus.ACTIVE,
                                "SKU-1",
                                OrderQuestionnaireAuditFactory.createNew(
                                                Id.withId("33333333-3333-3333-3333-333333333333"),
                                                "REF-Q",
                                                "Question Creator",
                                                "question@acme.com",
                                                LocalDateTime.parse("2026-01-01T09:00:00")
                                        )
                                        .getOrElseThrow(error -> new IllegalStateException("Invalid audit test data: " + error))
                        ),
                        AnswerConfigurationFactory.createTextStrategy(),
                        0
                ))
                : List.of();

        return Questionnaire.rehydrate(
                QuestionnaireId.of(command.id(), command.channelDistributionId(), command.journeyDistributionId()),
                "Questionnaire " + command.id(),
                status,
                configuredQuestions,
                OrderQuestionnaireAuditFactory.createNew(
                                Id.withId("11111111-1111-1111-1111-111111111111"),
                                "REF",
                                "Creator",
                                "creator@acme.com",
                                LocalDateTime.parse("2026-01-01T10:00:00")
                        )
                        .getOrElseThrow(error -> new IllegalStateException("Invalid audit test data: " + error))
        );
    }
}

