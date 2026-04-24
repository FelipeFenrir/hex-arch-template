package com.acme.orderquestionnaire.application.questionnaire.bdd;

import com.acme.orderquestionnaire.application.audit.dto.command.AuditUserParam;
import com.acme.orderquestionnaire.application.question.dto.command.UpdateQuestionCommand;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionUpdatedView;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.question.service.UpdateQuestionService;
import com.acme.orderquestionnaire.application.question.service.UpdateQuestionServiceTest;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.question.Question;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@BddTestSteps
public class UpdateQuestionStepDefs {

    private QuestionCommandOutPort repository;
    private UpdateQuestionService service;

    private String id;
    private UpdateQuestionCommand command;
    private Result<QuestionUpdatedView, List<DomainError>> result;
    private QuestionUpdatedView successView;

    @Before
    public void setUp() {
        repository = mock(QuestionCommandOutPort.class);
        service = UpdateQuestionServiceTest.buildService(repository);
        id = null;
        command = null;
        result = null;
        successView = null;

        when(repository.update(any(Question.class))).thenAnswer(invocation -> Result.success(invocation.getArgument(0)));
    }

    @Given("an existing question {string} with status {string}")
    public void anExistingQuestionWithStatus(String questionId, String status) {
        when(repository.findQuestionById(questionId)).thenReturn(Optional.of(question(questionId, status)));
    }

    @Given("no existing question {string} for update")
    public void noExistingQuestionForUpdate(String questionId) {
        when(repository.findQuestionById(questionId)).thenReturn(Optional.empty());
    }

    @And("an update question request for id {string} with label {string}, sales item {string} and status {string}")
    public void anUpdateQuestionRequestForIdWithLabelSalesItemAndStatus(String questionId,
                                                                         String label,
                                                                         String salesItemReferenceCode,
                                                                         String status) {
        id = questionId;
        command = new UpdateQuestionCommand(
                label,
                salesItemReferenceCode,
                "KEEP".equals(status) ? null : ParameterizationStatus.valueOf(status),
                updatedBy(),
                updatedAt()
        );
    }

    @When("I execute the update question use case")
    public void iExecuteTheUpdateQuestionUseCase() {
        result = service.execute(id, command);
    }

    @Then("the update question result should be a success")
    public void theUpdateQuestionResultShouldBeASuccess() {
        assertInstanceOf(Result.Success.class, result);
        successView = result.getOrElseThrow(errors ->
                new IllegalStateException("Expected success but got failure: " + errors));
    }

    @And("the updated question view should have status {string} and label {string}")
    public void theUpdatedQuestionViewShouldHaveStatusAndLabel(String status, String label) {
        assertEquals(status, successView.status());
        assertEquals(label, successView.label());
        verify(repository).update(any(Question.class));
    }

    @Then("the update question result should fail with code {string}")
    public void theUpdateQuestionResultShouldFailWithCode(String code) {
        assertInstanceOf(Result.Failure.class, result);
        List<DomainError> errors = result.errorOrElseThrow(() ->
                new IllegalStateException("Expected failure but got success"));
        assertTrue(errors.stream().anyMatch(error -> error.code().equals(code)));
        verify(repository, never()).update(any(Question.class));
    }

    private static Question question(String id, String status) {
        return Question.rehydrate(
                id,
                "Label " + id,
                ParameterizationStatus.valueOf(status),
                "SKU-1",
                OrderQuestionnaireAuditFactory.createNew(
                                Id.withId("33333333-3333-3333-3333-333333333333"),
                                "REF-CREATE",
                                "Creator",
                                "create@acme.com",
                                LocalDateTime.parse("2026-01-10T08:00:00")
                        )
                        .getOrElseThrow(error -> new IllegalStateException("Invalid audit test data: " + error))
        );
    }

    private static AuditUserParam updatedBy() {
        return new AuditUserParam(
                Id.withId("44444444-4444-4444-4444-444444444444"),
                "REF-UPD",
                "Updater",
                "update@acme.com"
        );
    }

    private static LocalDateTime updatedAt() {
        return LocalDateTime.parse("2026-01-10T09:00:00");
    }
}

