package com.acme.orderquestionnaire.application.bdd;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.orderquestionnaire.application.channel.port.out.ChannelDistributionOutPort;
import com.acme.orderquestionnaire.application.journey.port.out.JourneyDistributionOutPort;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.CreateQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireCreatedView;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.CreateQuestionnaireService;
import com.acme.orderquestionnaire.application.unit.questionnaire.service.CreateQuestionnaireServiceTest;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@BddTestSteps
public class CreateQuestionnaireStepDefs {

    private QuestionnaireCommandOutPort questionnaireRepository;
    private ChannelDistributionOutPort channelPort;
    private JourneyDistributionOutPort journeyPort;
    private CreateQuestionnaireService service;

    private CreateQuestionnaireCommand command;
    private Result<QuestionnaireCreatedView, List<DomainError>> result;
    private QuestionnaireCreatedView successView;

    @Before
    public void setUp() {
        questionnaireRepository = mock(QuestionnaireCommandOutPort.class);
        channelPort = mock(ChannelDistributionOutPort.class);
        journeyPort = mock(JourneyDistributionOutPort.class);
        service = CreateQuestionnaireServiceTest.buildService(questionnaireRepository, channelPort, journeyPort);

        command = null;
        result = null;
        successView = null;

        when(questionnaireRepository.create(any(Questionnaire.class)))
                .thenAnswer(invocation -> Result.success(invocation.getArgument(0)));
    }

    @Given("channel {string} exists for questionnaire creation")
    public void channelExistsForQuestionnaireCreation(String channel) {
        when(channelPort.existsById(channel)).thenReturn(true);
    }

    @Given("channel {string} does not exist for questionnaire creation")
    public void channelDoesNotExistForQuestionnaireCreation(String channel) {
        when(channelPort.existsById(channel)).thenReturn(false);
    }

    @Given("journey {string} exists for questionnaire creation")
    public void journeyExistsForQuestionnaireCreation(String journey) {
        when(journeyPort.existsById(journey)).thenReturn(true);
    }

    @Given("journey {string} does not exist for questionnaire creation")
    public void journeyDoesNotExistForQuestionnaireCreation(String journey) {
        when(journeyPort.existsById(journey)).thenReturn(false);
    }

    @Given("no questionnaire exists with id {string}, channel {string} and journey {string}")
    public void noQuestionnaireExists(String id, String channel, String journey) {
        when(questionnaireRepository.existsById(QuestionnaireId.of(id, channel, journey))).thenReturn(false);
    }

    @Given("questionnaire already exists with id {string}, channel {string} and journey {string}")
    public void questionnaireAlreadyExists(String id, String channel, String journey) {
        when(questionnaireRepository.existsById(QuestionnaireId.of(id, channel, journey))).thenReturn(true);
    }

    @And("a create questionnaire command with id {string}, channel {string}, journey {string} and description {string}")
    public void aCreateQuestionnaireCommand(String id, String channel, String journey, String description) {
        command = new CreateQuestionnaireCommand(id, channel, journey, description, defaultUser(), defaultNow());
    }

    @And("a create questionnaire command with id {string}, channel {string}, journey {string}, description {string} and missing audit info")
    public void aCreateQuestionnaireCommandWithMissingAuditInfo(String id, String channel, String journey, String description) {
        command = new CreateQuestionnaireCommand(id, channel, journey, description, null, null);
    }

    @When("I execute the create questionnaire use case")
    public void iExecuteTheCreateQuestionnaireUseCase() {
        result = service.execute(command);
    }

    @Then("the create questionnaire result should be a success")
    public void theCreateQuestionnaireResultShouldBeASuccess() {
        assertInstanceOf(Result.Success.class, result);
        successView = result.getOrElseThrow(errors ->
                new IllegalStateException("Expected success but got failure: " + errors));
    }

    @And("the created questionnaire view should have id {string} and status {string}")
    public void theCreatedQuestionnaireViewShouldHaveIdAndStatus(String id, String status) {
        assertEquals(id, successView.id());
        assertEquals(status, successView.status());
        verify(questionnaireRepository).create(any(Questionnaire.class));
    }

    @Then("the create questionnaire result should fail with code {string}")
    public void theCreateQuestionnaireResultShouldFailWithCode(String code) {
        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals(code)));
        verify(questionnaireRepository, never()).create(any(Questionnaire.class));
    }

    @And("create questionnaire should short-circuit after channel validation")
    public void createQuestionnaireShouldShortCircuitAfterChannelValidation() {
        verify(channelPort).existsById("APP");
        verify(journeyPort, never()).existsById(anyString());
        verify(questionnaireRepository, never()).existsById(any(QuestionnaireId.class));
    }

    @And("create questionnaire should short-circuit after journey validation")
    public void createQuestionnaireShouldShortCircuitAfterJourneyValidation() {
        verify(channelPort).existsById("APP");
        verify(journeyPort).existsById("JOURNEY_01");
        verify(questionnaireRepository, never()).existsById(any(QuestionnaireId.class));
    }

    private static AuditUserParam defaultUser() {
        return new AuditUserParam(
                Id.withId("00000000-0000-0000-0000-000000000001"),
                "REF-1",
                "Test User",
                "test@acme.com"
        );
    }

    private static LocalDateTime defaultNow() {
        return LocalDateTime.parse("2026-01-01T10:00:00");
    }
}


