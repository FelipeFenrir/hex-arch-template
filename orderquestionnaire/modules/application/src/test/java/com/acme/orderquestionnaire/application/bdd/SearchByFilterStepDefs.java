package com.acme.orderquestionnaire.application.bdd;

import com.acme.orderquestionnaire.application.question.dto.queries.SearchQuestionByFilter;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionView;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionQueryOutPort;
import com.acme.orderquestionnaire.application.question.service.SearchQuestionByFilterHandler;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.SearchQuestionnaireByFilter;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireView;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireQueryOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.SearchQuestionnaireByFilterHandler;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageMode;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.engine.pagination.SortDirection;
import com.acme.shared.engine.pagination.SortSpec;
import com.acme.shared.stereotypes.test.BddTestSteps;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@BddTestSteps
public class SearchByFilterStepDefs {

    private QuestionQueryOutPort questionQueryOutPort;
    private QuestionnaireQueryOutPort questionnaireQueryOutPort;

    private SearchQuestionByFilterHandler searchQuestionByFilterHandler;
    private SearchQuestionnaireByFilterHandler searchQuestionnaireByFilterHandler;

    private SearchQuestionByFilter questionQuery;
    private SearchQuestionnaireByFilter questionnaireQuery;
    private PageResult<?> result;
    private IllegalArgumentException invalidHybridRequestError;

    @Before
    public void setUp() {
        questionQueryOutPort = mock(QuestionQueryOutPort.class);
        questionnaireQueryOutPort = mock(QuestionnaireQueryOutPort.class);
        searchQuestionByFilterHandler = new SearchQuestionByFilterHandler(questionQueryOutPort);
        searchQuestionnaireByFilterHandler = new SearchQuestionnaireByFilterHandler(questionnaireQueryOutPort);

        questionQuery = null;
        questionnaireQuery = null;
        result = null;
        invalidHybridRequestError = null;
    }

    @Given("a question search filter using page {int} and size {int}")
    public void aQuestionSearchFilterUsingPageAndSize(int page, int size) {
        questionQuery = new SearchQuestionByFilter(
                null,
                null,
                null,
                null,
                null,
                HybridPageRequest.ofPage(page, size, List.of(new SortSpec("id", SortDirection.ASC)))
        );
    }

    @Given("a question search filter using cursor {string} and size {int}")
    public void aQuestionSearchFilterUsingCursorAndSize(String cursor, int size) {
        questionQuery = new SearchQuestionByFilter(
                null,
                null,
                null,
                null,
                null,
                HybridPageRequest.ofCursor(cursor, size, List.of(new SortSpec("createdAt", SortDirection.DESC)))
        );
    }

    @Given("a questionnaire search filter using page {int} and size {int}")
    public void aQuestionnaireSearchFilterUsingPageAndSize(int page, int size) {
        questionnaireQuery = new SearchQuestionnaireByFilter(
                null,
                null,
                null,
                null,
                null,
                null,
                HybridPageRequest.ofPage(page, size, List.of(new SortSpec("description", SortDirection.ASC)))
        );
    }

    @Given("a questionnaire search filter using cursor {string} and size {int}")
    public void aQuestionnaireSearchFilterUsingCursorAndSize(String cursor, int size) {
        questionnaireQuery = new SearchQuestionnaireByFilter(
                null,
                null,
                null,
                null,
                null,
                null,
                HybridPageRequest.ofCursor(cursor, size, List.of(new SortSpec("updatedAt", SortDirection.DESC)))
        );
    }

    @And("the question repository returns one paged item sorted by {string} in {string}")
    public void theQuestionRepositoryReturnsOnePagedItemSortedByIn(String field, String direction) {
        var sortDirection = SortDirection.valueOf(direction);
        var view = mock(QuestionView.class);
        when(questionQueryOutPort.findAll(any(SearchQuestionByFilter.class))).thenReturn(
                PageResult.forPage(
                        List.of(view),
                        0,
                        10,
                        1,
                        1,
                        true,
                        true,
                        List.of(new SortSpec(field, sortDirection))
                )
        );
    }

    @And("the question repository returns cursor {string} with hasNext {word} sorted by {string} in {string}")
    public void theQuestionRepositoryReturnsCursorWithHasNextSortedByIn(
            String nextCursor,
            String hasNext,
            String field,
            String direction
    ) {
        var sortDirection = SortDirection.valueOf(direction);
        when(questionQueryOutPort.findAll(any(SearchQuestionByFilter.class))).thenReturn(
                PageResult.forCursor(
                        List.of(),
                        5,
                        nextCursor,
                        Boolean.parseBoolean(hasNext),
                        List.of(new SortSpec(field, sortDirection))
                )
        );
    }

    @And("the questionnaire repository returns one paged item sorted by {string} in {string}")
    public void theQuestionnaireRepositoryReturnsOnePagedItemSortedByIn(String field, String direction) {
        var sortDirection = SortDirection.valueOf(direction);
        var view = mock(QuestionnaireView.class);
        when(questionnaireQueryOutPort.findAll(any(SearchQuestionnaireByFilter.class))).thenReturn(
                PageResult.forPage(
                        List.of(view),
                        1,
                        20,
                        1,
                        1,
                        true,
                        true,
                        List.of(new SortSpec(field, sortDirection))
                )
        );
    }

    @And("the questionnaire repository returns cursor {string} with hasNext {word} sorted by {string} in {string}")
    public void theQuestionnaireRepositoryReturnsCursorWithHasNextSortedByIn(
            String nextCursor,
            String hasNext,
            String field,
            String direction
    ) {
        var sortDirection = SortDirection.valueOf(direction);
        when(questionnaireQueryOutPort.findAll(any(SearchQuestionnaireByFilter.class))).thenReturn(
                PageResult.forCursor(
                        List.of(),
                        7,
                        nextCursor,
                        Boolean.parseBoolean(hasNext),
                        List.of(new SortSpec(field, sortDirection))
                )
        );
    }

    @When("I execute the question search handler")
    public void iExecuteTheQuestionSearchHandler() {
        result = searchQuestionByFilterHandler.execute(questionQuery);
    }

    @When("I execute the questionnaire search handler")
    public void iExecuteTheQuestionnaireSearchHandler() {
        result = searchQuestionnaireByFilterHandler.execute(questionnaireQuery);
    }

    @When("I create a hybrid page request with page {int} and cursor {string}")
    public void iCreateAHybridPageRequestWithPageAndCursor(int page, String cursor) {
        try {
            new HybridPageRequest(page, 10, cursor, List.of(new SortSpec("id", SortDirection.ASC)));
        } catch (IllegalArgumentException e) {
            invalidHybridRequestError = e;
        }
    }

    @Then("the paged result mode should be {string}")
    public void thePagedResultModeShouldBe(String expectedMode) {
        assertNotNull(result);
        assertEquals(PageMode.valueOf(expectedMode), result.mode());
    }

    @Then("the paged result should contain {int} item")
    public void thePagedResultShouldContainItem(int expectedItems) {
        assertNotNull(result);
        assertEquals(expectedItems, result.content().size());
    }

    @Then("the applied sort should include field {string} and direction {string}")
    public void theAppliedSortShouldIncludeFieldAndDirection(String field, String direction) {
        assertNotNull(result);
        var expectedDirection = SortDirection.valueOf(direction);
        assertTrue(result.appliedSort().stream().anyMatch(sort ->
                sort.field().equals(field) && sort.direction() == expectedDirection
        ));
    }

    @Then("the next cursor should be {string}")
    public void theNextCursorShouldBe(String expectedNextCursor) {
        assertNotNull(result);
        assertEquals(expectedNextCursor, result.nextCursor());
    }

    @Then("has next should be {word}")
    public void hasNextShouldBe(String expectedHasNext) {
        assertNotNull(result);
        assertEquals(Boolean.parseBoolean(expectedHasNext), result.hasNext());
    }

    @Then("the hybrid request creation should fail with message {string}")
    public void theHybridRequestCreationShouldFailWithMessage(String expectedMessage) {
        assertNotNull(invalidHybridRequestError);
        assertEquals(expectedMessage, invalidHybridRequestError.getMessage());
    }
}


