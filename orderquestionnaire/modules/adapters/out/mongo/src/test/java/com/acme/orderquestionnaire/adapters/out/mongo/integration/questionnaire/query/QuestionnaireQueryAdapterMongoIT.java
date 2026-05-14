package com.acme.orderquestionnaire.adapters.out.mongo.integration.questionnaire.query;

import com.acme.orderquestionnaire.adapters.out.mongo.integration.support.AbstractQuestionnaireQueryMongoIT;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.QuestionnaireQueryAdapter;
import com.acme.orderquestionnaire.adapters.out.mongo.question.mapper.QuestionEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.unit.support.MongoTestDataFactory;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.GetQuestionnaireById;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.SearchQuestionnaireByFilter;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireFactory;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageMode;
import com.acme.shared.engine.pagination.SortDirection;
import com.acme.shared.engine.pagination.SortSpec;
import com.acme.shared.stereotypes.test.IntegrationTest;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@IntegrationTest
@DisplayName("QuestionnaireQueryAdapter Mongo Integration Test")
class QuestionnaireQueryAdapterMongoIT extends AbstractQuestionnaireQueryMongoIT {

    @Autowired
    private QuestionnaireQueryAdapter questionnaireQueryAdapter;

    @Autowired
    private QuestionEntityMapper questionEntityMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

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

    @Test
    void shouldHonorRequestedSortWhenUsingCursorMode() {
        questionnaireCommandRepository.save(
                questionnaireEntityMapper.toEntity(newQuestionnaire("qn_sort_cursor_3_it", "channel_1", "journey_1", "Gamma")));
        questionnaireCommandRepository.save(
                questionnaireEntityMapper.toEntity(newQuestionnaire("qn_sort_cursor_1_it", "channel_1", "journey_1", "Alpha")));
        questionnaireCommandRepository.save(
                questionnaireEntityMapper.toEntity(newQuestionnaire("qn_sort_cursor_2_it", "channel_1", "journey_1", "Beta")));

        var firstPageCriteria = new SearchQuestionnaireByFilter(
                null,
                null,
                null,
                null,
                null,
                null,
                HybridPageRequest.ofCursorStart(2, List.of(new SortSpec("description", SortDirection.ASC)))
        );

        var firstPage = questionnaireQueryAdapter.findAll(firstPageCriteria);

        assertEquals(PageMode.CURSOR, firstPage.mode());
        assertEquals(List.of("qn_sort_cursor_1_it", "qn_sort_cursor_2_it"),
                firstPage.content().stream().map(view -> view.questionnaireId().id()).toList());
        assertTrue(firstPage.hasNext());
        assertEquals("qn_sort_cursor_2_it", firstPage.nextCursor());
        assertEquals("description", firstPage.appliedSort().getFirst().field());

        var secondPageCriteria = new SearchQuestionnaireByFilter(
                null,
                null,
                null,
                null,
                null,
                null,
                HybridPageRequest.ofCursor(firstPage.nextCursor(), 2, List.of(new SortSpec("description", SortDirection.ASC)))
        );

        var secondPage = questionnaireQueryAdapter.findAll(secondPageCriteria);

        assertEquals(List.of("qn_sort_cursor_3_it"),
                secondPage.content().stream().map(view -> view.questionnaireId().id()).toList());
        assertFalse(secondPage.hasNext());
        assertNull(secondPage.nextCursor());
    }

    @Test
    void shouldReadLegacyClassBasedRootConditionPayload() {
        var questionnaire = MongoTestDataFactory.newQuestionnaire("qn_query_legacy_root_it", "channel_1", "journey_1");
        var question = MongoTestDataFactory.newQuestion("question_legacy_root_it");

        questionnaireCommandRepository.save(questionnaireEntityMapper.toEntity(questionnaire));
        mongoTemplate.save(questionEntityMapper.toEntity(question));

        Document rootCondition = new Document("_class", "com.acme.orderquestionnaire.domain.questionnaire.conditioner.EqualCondition")
                .append("questionRootCode", new Document("value", "question_anchor"))
                .append("expectedValue", "YES");

        Document link = new Document("_id", questionnaire.id() + "|" + question.id())
                .append("questionnaire_document_id", questionnaire.id() + "|" + questionnaire.channelDistributionId() + "|" + questionnaire.journeyDistributionId())
                .append("questionnaire_id", questionnaire.id())
                .append("channel_distribution_id", questionnaire.channelDistributionId())
                .append("journey_distribution_id", questionnaire.journeyDistributionId())
                .append("question_id", question.id())
                .append("order", 0)
                .append("root_condition", rootCondition);

        mongoTemplate.insert(link, "questionnaire_questions");

        var found = questionnaireQueryAdapter.findById(
                new GetQuestionnaireById(QuestionnaireId.of(questionnaire.id(), questionnaire.channelDistributionId(), questionnaire.journeyDistributionId())));

        assertTrue(found.isPresent());
        assertEquals(1, found.get().configuredQuestions().size());
        assertEquals("EQUAL", found.get().configuredQuestions().getFirst().rootCondition().type());
        assertEquals("question_anchor", found.get().configuredQuestions().getFirst().rootCondition().attributes().get("questionRootCode"));
    }

    private com.acme.orderquestionnaire.domain.questionnaire.Questionnaire newQuestionnaire(
            String id,
            String channelId,
            String journeyId,
            String description
    ) {
        return QuestionnaireFactory.createNew(id, channelId, journeyId, description, MongoTestDataFactory.createdAuditInfo())
                .flatMap(QuestionnaireFactory.QuestionnaireBuilder::build)
                .getOrElseThrow(errors -> new IllegalStateException("Invalid fixture questionnaire: " + errors));
    }
}

