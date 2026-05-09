package com.acme.orderquestionnaire.adapters.out.mongo.integration.question.query;

import com.acme.orderquestionnaire.adapters.out.mongo.integration.support.AbstractQuestionQueryMongoIT;
import com.acme.orderquestionnaire.adapters.out.mongo.question.QuestionQueryAdapter;
import com.acme.orderquestionnaire.adapters.out.mongo.unit.support.MongoTestDataFactory;
import com.acme.orderquestionnaire.application.question.dto.queries.GetQuestionById;
import com.acme.orderquestionnaire.application.question.dto.queries.SearchQuestionByFilter;
import com.acme.orderquestionnaire.domain.question.QuestionFactory;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageMode;
import com.acme.shared.engine.pagination.SortDirection;
import com.acme.shared.engine.pagination.SortSpec;
import com.acme.shared.stereotypes.test.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@IntegrationTest
@DisplayName("QuestionQueryAdapter Mongo Integration Test")
class QuestionQueryAdapterMongoIT extends AbstractQuestionQueryMongoIT {

    @Autowired
    private QuestionQueryAdapter questionQueryAdapter;

    @Test
    void shouldFindById() {
        var question = MongoTestDataFactory.newQuestion("question_query_find_it");
        questionCommandRepository.save(questionEntityMapper.toEntity(question));

        var found = questionQueryAdapter.findById(new GetQuestionById("question_query_find_it"));

        assertTrue(found.isPresent());
        assertEquals("question_query_find_it", found.get().id());
        assertEquals(question.label(), found.get().label());
    }

    @Test
    void shouldReturnEmptyWhenFindByIdDoesNotExist() {
        var found = questionQueryAdapter.findById(new GetQuestionById("question_missing_it"));
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldFindAllByPageWithStatusFilter() {
        questionCommandRepository.save(questionEntityMapper.toEntity(MongoTestDataFactory.newQuestion("question_query_page_1_it")));
        questionCommandRepository.save(questionEntityMapper.toEntity(MongoTestDataFactory.rehydratedActiveQuestion("question_query_page_2_it")));

        var criteria = new SearchQuestionByFilter(
                null,
                null,
                "question",
                com.acme.shared.enumerator.ParameterizationStatus.ACTIVE,
                null,
                HybridPageRequest.ofPage(0, 10, List.of(new SortSpec("id", SortDirection.ASC)))
        );

        var page = questionQueryAdapter.findAll(criteria);

        assertEquals(PageMode.PAGE, page.mode());
        assertEquals(1, page.content().size());
        assertEquals("question_query_page_2_it", page.content().getFirst().id());
        assertEquals(1L, page.totalElements());
    }

    @Test
    void shouldFindAllByCursor() {
        questionCommandRepository.save(questionEntityMapper.toEntity(MongoTestDataFactory.newQuestion("question_query_cursor_1_it")));
        questionCommandRepository.save(questionEntityMapper.toEntity(MongoTestDataFactory.newQuestion("question_query_cursor_2_it")));
        questionCommandRepository.save(questionEntityMapper.toEntity(MongoTestDataFactory.newQuestion("question_query_cursor_3_it")));

        var criteria = new SearchQuestionByFilter(
                null,
                null,
                null,
                null,
                null,
                HybridPageRequest.ofCursor("question_query_cursor_1_it", 1, List.of())
        );

        var page = questionQueryAdapter.findAll(criteria);

        assertEquals(PageMode.CURSOR, page.mode());
        assertEquals(1, page.content().size());
        assertEquals("question_query_cursor_2_it", page.content().getFirst().id());
        assertTrue(page.hasNext());
        assertEquals("question_query_cursor_2_it", page.nextCursor());
    }

    @Test
    void shouldHonorRequestedSortWhenUsingCursorMode() {
        questionCommandRepository.save(questionEntityMapper.toEntity(newQuestion("question_sort_cursor_3_it", "Gamma", "sales_item_code_3")));
        questionCommandRepository.save(questionEntityMapper.toEntity(newQuestion("question_sort_cursor_1_it", "Alpha", "sales_item_code_1")));
        questionCommandRepository.save(questionEntityMapper.toEntity(newQuestion("question_sort_cursor_2_it", "Beta", "sales_item_code_2")));

        var firstPageCriteria = new SearchQuestionByFilter(
                null,
                null,
                null,
                null,
                null,
                HybridPageRequest.ofCursorStart(2, List.of(new SortSpec("label", SortDirection.ASC)))
        );

        var firstPage = questionQueryAdapter.findAll(firstPageCriteria);

        assertEquals(PageMode.CURSOR, firstPage.mode());
        assertEquals(List.of("question_sort_cursor_1_it", "question_sort_cursor_2_it"),
                firstPage.content().stream().map(com.acme.orderquestionnaire.application.question.dto.view.QuestionView::id).toList());
        assertTrue(firstPage.hasNext());
        assertEquals("question_sort_cursor_2_it", firstPage.nextCursor());
        assertEquals("label", firstPage.appliedSort().getFirst().field());

        var secondPageCriteria = new SearchQuestionByFilter(
                null,
                null,
                null,
                null,
                null,
                HybridPageRequest.ofCursor(firstPage.nextCursor(), 2, List.of(new SortSpec("label", SortDirection.ASC)))
        );

        var secondPage = questionQueryAdapter.findAll(secondPageCriteria);

        assertEquals(List.of("question_sort_cursor_3_it"),
                secondPage.content().stream().map(com.acme.orderquestionnaire.application.question.dto.view.QuestionView::id).toList());
        assertFalse(secondPage.hasNext());
        assertNull(secondPage.nextCursor());
    }

    private com.acme.orderquestionnaire.domain.question.Question newQuestion(String id, String label, String salesItemReferenceCode) {
        return QuestionFactory.createNew(id, label, salesItemReferenceCode, MongoTestDataFactory.createdAuditInfo())
                .flatMap(QuestionFactory.NewQuestionBuilder::build)
                .getOrElseThrow(errors -> new IllegalStateException("Invalid fixture question: " + errors));
    }
}
