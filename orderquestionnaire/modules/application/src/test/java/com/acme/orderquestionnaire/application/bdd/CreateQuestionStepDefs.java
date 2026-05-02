package com.acme.orderquestionnaire.application.bdd;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.orderquestionnaire.application.question.dto.command.CreateQuestionCommand;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionCreatedView;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.question.service.CreateQuestionService;
import com.acme.orderquestionnaire.application.unit.question.service.CreateQuestionServiceTest;
import com.acme.orderquestionnaire.domain.question.Question;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@BddTestSteps
public class CreateQuestionStepDefs {

    private QuestionCommandOutPort repository;
    private CreateQuestionService service;

    private CreateQuestionCommand command;
    private Result<QuestionCreatedView, List<DomainError>> result;
    private QuestionCreatedView successView;

    @Before
    public void setUp() {
        repository = mock(QuestionCommandOutPort.class);
        service = CreateQuestionServiceTest.buildService(repository);
        command = null;
        result = null;
        successView = null;

        when(repository.create(any(Question.class))).thenAnswer(invocation -> Result.success(invocation.getArgument(0)));
    }

    @Given("no question exists with id {string} for creation")
    public void noQuestionExistsWithIdForCreation(String id) {
        when(repository.existsById(id)).thenReturn(false);
    }

    @Given("a question already exists with id {string} for creation")
    public void aQuestionAlreadyExistsWithIdForCreation(String id) {
        when(repository.existsById(id)).thenReturn(true);
    }

    @And("a create question command with id {string}, label {string} and sales item {string}")
    public void aCreateQuestionCommandWithIdLabelAndSalesItem(String id, String label, String salesItemReferenceCode) {
        command = new CreateQuestionCommand(id, label, salesItemReferenceCode, defaultUser(), defaultNow());
    }

    @And("a create question command with id {string}, label {string}, sales item {string} and missing audit info")
    public void aCreateQuestionCommandWithMissingAuditInfo(String id, String label, String salesItemReferenceCode) {
        command = new CreateQuestionCommand(id, label, salesItemReferenceCode, null, null);
    }

    @When("I execute the create question use case")
    public void iExecuteTheCreateQuestionUseCase() {
        result = service.execute(command);
    }

    @Then("the create question result should be a success")
    public void theCreateQuestionResultShouldBeASuccess() {
        assertInstanceOf(Result.Success.class, result);
        successView = result.getOrElseThrow(errors ->
                new IllegalStateException("Expected success but got failure: " + errors));
    }

    @And("the created question view should have id {string} and status {string}")
    public void theCreatedQuestionViewShouldHaveIdAndStatus(String id, String status) {
        assertEquals(id, successView.id());
        assertEquals(status, successView.status());
        verify(repository).create(any(Question.class));
    }

    @Then("the create question result should fail with code {string}")
    public void theCreateQuestionResultShouldFailWithCode(String code) {
        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals(code)));
        verify(repository, never()).create(any(Question.class));
    }

    private static AuditUserParam defaultUser() {
        return new AuditUserParam(
                Id.withId("11111111-1111-1111-1111-111111111111"),
                "REF-1",
                "Test User",
                "test@acme.com"
        );
    }

    private static LocalDateTime defaultNow() {
        return LocalDateTime.parse("2026-01-10T10:00:00");
    }
}

