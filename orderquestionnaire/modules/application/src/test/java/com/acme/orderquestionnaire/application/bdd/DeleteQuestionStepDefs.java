package com.acme.orderquestionnaire.application.bdd;

import com.acme.orderquestionnaire.application.question.dto.view.DeleteQuestionsResultView;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionCommandOutPort;
import com.acme.orderquestionnaire.application.question.service.DeleteQuestionService;
import com.acme.orderquestionnaire.application.question.service.context.DeleteQuestionPipelineContext;
import com.acme.orderquestionnaire.application.question.service.step.CheckQuestionNotInUseStep;
import com.acme.orderquestionnaire.application.question.service.step.DeleteQuestionStep;
import com.acme.orderquestionnaire.application.question.service.step.FetchQuestionForDeleteStep;
import com.acme.orderquestionnaire.application.question.service.step.ValidateDeleteQuestionIdStep;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.domain.audit.OrderQuestionnaireAuditFactory;
import com.acme.orderquestionnaire.domain.question.Question;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@BddTestSteps
public class DeleteQuestionStepDefs {

    private QuestionCommandOutPort questionRepository;
    private QuestionnaireCommandOutPort questionnaireRepository;
    private DeleteQuestionService service;

    private String singleId;
    private List<String> batchIds;
    private Result<Void, List<DomainError>> singleResult;
    private Result<DeleteQuestionsResultView, List<DomainError>> batchResult;

    @Before
    public void setUp() {
        questionRepository = mock(QuestionCommandOutPort.class);
        questionnaireRepository = mock(QuestionnaireCommandOutPort.class);
        service = buildService(questionRepository, questionnaireRepository);

        singleId = null;
        batchIds = null;
        singleResult = null;
        batchResult = null;

        when(questionRepository.deleteById(any())).thenReturn(Result.success(null));
        when(questionnaireRepository.findReferencingQuestionnaireIdsByQuestionIds(anyList())).thenReturn(Map.of());
    }

    @Given("question {string} exists for delete")
    public void questionExistsForDelete(String id) {
        when(questionRepository.findQuestionById(id)).thenReturn(Optional.of(question(id)));
    }

    @Given("question {string} does not exist for delete")
    public void questionDoesNotExistForDelete(String id) {
        when(questionRepository.findQuestionById(id)).thenReturn(Optional.empty());
    }

    @And("question {string} is linked to questionnaires {string}")
    public void questionIsLinkedToQuestionnaires(String questionId, String questionnaireIdsCsv) {
        List<String> questionnaireIds = List.of(questionnaireIdsCsv.split(","));
        when(questionnaireRepository.findReferencingQuestionnaireIdsByQuestionIds(anyList()))
                .thenReturn(Map.of(questionId, questionnaireIds));
    }

    @Given("a delete question request for id {string}")
    public void aDeleteQuestionRequestForId(String id) {
        singleId = id;
    }

    @Given("a delete questions batch request with ids {string}")
    public void aDeleteQuestionsBatchRequestWithIds(String idsCsv) {
        batchIds = List.of(idsCsv.split(","));
    }

    @When("I execute delete question")
    public void iExecuteDeleteQuestion() {
        singleResult = service.execute(singleId);
    }

    @When("I execute delete questions batch")
    public void iExecuteDeleteQuestionsBatch() {
        batchResult = service.execute(batchIds);
    }

    @Then("delete question should succeed")
    public void deleteQuestionShouldSucceed() {
        assertInstanceOf(Result.Success.class, singleResult);
        verify(questionRepository).deleteById(singleId);
    }

    @Then("delete question should fail with code {string}")
    public void deleteQuestionShouldFailWithCode(String code) {
        assertInstanceOf(Result.Failure.class, singleResult);
        Result.Failure<Void, List<DomainError>> failure = (Result.Failure<Void, List<DomainError>>) singleResult;
        assertTrue(failure.error().stream().anyMatch(error -> error.code().equals(code)));
        verify(questionRepository, never()).deleteById(any());
    }

    @Then("delete batch should succeed with {int} failures")
    public void deleteBatchShouldSucceedWithFailures(int expectedFailures) {
        assertInstanceOf(Result.Success.class, batchResult);
        DeleteQuestionsResultView view = batchResult.getOrElseThrow(errors ->
                new IllegalStateException("Expected success but got failure: " + errors));
        assertEquals(expectedFailures, view.failures().size());
    }

    @And("delete batch should contain question {string} with code {string}")
    public void deleteBatchShouldContainQuestionWithCode(String questionId, String code) {
        DeleteQuestionsResultView view = batchResult.getOrElseThrow(errors ->
                new IllegalStateException("Expected success but got failure: " + errors));
        assertTrue(view.failures().stream().anyMatch(failure ->
                failure.questionId().equals(questionId)
                        && failure.code().equals(code)));
    }

    private static DeleteQuestionService buildService(QuestionCommandOutPort questionRepo,
                                                      QuestionnaireCommandOutPort questionnaireRepo) {
        List<Step<DeleteQuestionPipelineContext>> steps = List.of(
                new ValidateDeleteQuestionIdStep(),
                new FetchQuestionForDeleteStep(questionRepo),
                new CheckQuestionNotInUseStep(questionnaireRepo),
                new DeleteQuestionStep(questionRepo)
        );
        return new DeleteQuestionService(steps);
    }

    private static Question question(String id) {
        return Question.rehydrate(
                id,
                "Label " + id,
                ParameterizationStatus.ACTIVE,
                "SKU-1",
                OrderQuestionnaireAuditFactory.createNew(
                                Id.withId("88888888-8888-8888-8888-888888888888"),
                                "REF",
                                "Creator",
                                "creator@acme.com",
                                LocalDateTime.parse("2026-01-01T10:00:00")
                        )
                        .getOrElseThrow(error -> new IllegalStateException("Invalid audit test data: " + error))
        );
    }
}

