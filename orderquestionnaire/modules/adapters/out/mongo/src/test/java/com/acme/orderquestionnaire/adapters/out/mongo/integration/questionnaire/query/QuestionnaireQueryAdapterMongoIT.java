package com.acme.orderquestionnaire.adapters.out.mongo.integration.questionnaire.query;

import com.acme.orderquestionnaire.adapters.out.mongo.integration.support.AbstractQuestionnaireQueryMongoIT;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.QuestionnaireQueryAdapter;
import com.acme.orderquestionnaire.adapters.out.mongo.unit.support.MongoTestDataFactory;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.GetQuestionnaireById;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.SearchQuestionnaireByFilter;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@IntegrationTest
@DisplayName("QuestionnaireQueryAdapter Mongo Integration Test")
class QuestionnaireQueryAdapterMongoIT extends AbstractQuestionnaireQueryMongoIT {

    @Autowired
    private QuestionnaireQueryAdapter questionnaireQueryAdapter;

    @Test
    void shouldFindById() {
        var questionnaire = MongoTestDataFactory.newQuestionnaire("qn_query_find_it", "channel_1", "journey_1");
        questionnaireCommandRepository.save(questionnaireEntityMapper.toEntity(questionnaire));

        var found = questionnaireQueryAdapter.findById(
                new GetQuestionnaireById(QuestionnaireId.of("qn_query_find_it", "channel_1", "journey_1")));

        assertTrue(found.isPresent());
        assertEquals("qn_query_find_it", found.get().questionnaireId().id());
        assertEquals(questionnaire.description(), found.get().description());
    }

    @Test
    void shouldReturnEmptyWhenFindByIdDoesNotExist() {
        var found = questionnaireQueryAdapter.findById(
                new GetQuestionnaireById(QuestionnaireId.of("qn_missing_it", "channel_1", "journey_1")));
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldFindAllByPageWithStatusFilter() {
        questionnaireCommandRepository.save(
                questionnaireEntityMapper.toEntity(MongoTestDataFactory.newQuestionnaire("qn_query_page_1_it", "channel_1", "journey_1")));
        questionnaireCommandRepository.save(
                questionnaireEntityMapper.toEntity(MongoTestDataFactory.rehydratedActiveQuestionnaire("qn_query_page_2_it", "channel_1", "journey_1")));

        var criteria = new SearchQuestionnaireByFilter(
                null,
                List.of("channel_1"),
                null,
                "Questionnaire",
                com.acme.shared.enumerator.ParameterizationStatus.ACTIVE,
                null,
                HybridPageRequest.ofPage(0, 10, List.of(new SortSpec("id", SortDirection.ASC)))
        );

        var page = questionnaireQueryAdapter.findAll(criteria);

        assertEquals(PageMode.PAGE, page.mode());
        assertEquals(1, page.content().size());
        assertEquals("qn_query_page_2_it", page.content().getFirst().questionnaireId().id());
        assertEquals(1L, page.totalElements());
    }

    @Test
    void shouldFindAllByCursor() {
        questionnaireCommandRepository.save(
                questionnaireEntityMapper.toEntity(MongoTestDataFactory.newQuestionnaire("qn_query_cursor_1_it", "channel_1", "journey_1")));
        questionnaireCommandRepository.save(
                questionnaireEntityMapper.toEntity(MongoTestDataFactory.newQuestionnaire("qn_query_cursor_2_it", "channel_1", "journey_1")));
        questionnaireCommandRepository.save(
                questionnaireEntityMapper.toEntity(MongoTestDataFactory.newQuestionnaire("qn_query_cursor_3_it", "channel_1", "journey_1")));

        var criteria = new SearchQuestionnaireByFilter(
                null,
                null,
                null,
                null,
                null,
                null,
                HybridPageRequest.ofCursor("qn_query_cursor_1_it", 1, List.of())
        );

        var page = questionnaireQueryAdapter.findAll(criteria);

        assertEquals(PageMode.CURSOR, page.mode());
        assertEquals(1, page.content().size());
        assertEquals("qn_query_cursor_2_it", page.content().getFirst().questionnaireId().id());
        assertTrue(page.hasNext());
        assertEquals("qn_query_cursor_2_it", page.nextCursor());
    }
}

