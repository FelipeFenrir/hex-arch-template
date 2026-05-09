package com.acme.orderquestionnaire.adapters.out.mongo.unit.questionnaire.query;

import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.QuestionnaireQueryAdapter;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.entity.QuestionnaireEntity;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.entity.QuestionnaireQuestionEntity;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.mapper.QuestionnaireEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.mapper.QuestionnaireQuestionEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.repository.QuestionnaireQueryRepository;
import com.acme.orderquestionnaire.adapters.out.mongo.question.mapper.QuestionEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.unit.support.MongoTestDataFactory;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.GetQuestionnaireById;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.SearchQuestionnaireByFilter;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageMode;
import com.acme.shared.engine.pagination.SortDirection;
import com.acme.shared.engine.pagination.SortSpec;
import com.acme.shared.stereotypes.test.UnitTest;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("QuestionnaireQueryAdapter")
class QuestionnaireQueryAdapterTest {

    @Mock
    private QuestionnaireQueryRepository questionnaireQueryRepository;

    @Mock
    private QuestionnaireEntityMapper questionnaireEntityMapper;

    @Mock
    private QuestionnaireQuestionEntityMapper questionnaireQuestionEntityMapper;

    @Mock
    private QuestionEntityMapper questionEntityMapper;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private QuestionnaireQueryAdapter adapter;

    // ── Read operations by id ─────────────────────────────────────────────────

    @Test
    @DisplayName("should find by id")
    void shouldFindById() {
        var questionnaire = MongoTestDataFactory.newQuestionnaire("qn_query_find", "channel_1", "journey_1");
        var entity = new QuestionnaireEntity(
                "qn_query_find|channel_1|journey_1",
                questionnaire.id(),
                questionnaire.channelDistributionId(),
                questionnaire.journeyDistributionId(),
                questionnaire.description(),
                questionnaire.status(),
                null,
                0
        );

        when(questionnaireEntityMapper.toDocumentId("qn_query_find", "channel_1", "journey_1"))
                .thenReturn("qn_query_find|channel_1|journey_1");
        when(questionnaireQueryRepository.findById("qn_query_find|channel_1|journey_1")).thenReturn(Optional.of(entity));
        when(mongoTemplate.find(any(Query.class), eq(QuestionnaireQuestionEntity.class))).thenReturn(List.of());
        when(questionnaireQuestionEntityMapper.toConfiguredQuestions(eq(List.of()), any(Map.class))).thenReturn(List.of());
        when(questionnaireEntityMapper.toDomain(eq(entity), any(List.class))).thenReturn(questionnaire);

        var found = adapter.findById(new GetQuestionnaireById(QuestionnaireId.of("qn_query_find", "channel_1", "journey_1")));

        assertTrue(found.isPresent());
        assertEquals("qn_query_find", found.get().questionnaireId().id());
    }

    @Test
    @DisplayName("should return empty when questionnaire id is missing")
    void shouldReturnEmptyWhenIdIsMissing() {
        var found = adapter.findById(new GetQuestionnaireById(null));
        assertTrue(found.isEmpty());
    }

    // ── Search operations ───────────────────────────────────────────────────

    @Test
    @DisplayName("should find all using page mode")
    void shouldFindAllUsingPageMode() {
        var questionnaire = MongoTestDataFactory.newQuestionnaire("qn_query_page_1", "channel_1", "journey_1");
        var entity = new QuestionnaireEntity(
                "qn_query_page_1|channel_1|journey_1",
                questionnaire.id(),
                questionnaire.channelDistributionId(),
                questionnaire.journeyDistributionId(),
                questionnaire.description(),
                questionnaire.status(),
                null,
                0
        );

        when(mongoTemplate.find(any(Query.class), eq(QuestionnaireEntity.class))).thenReturn(List.of(entity));
        when(mongoTemplate.find(any(Query.class), eq(QuestionnaireQuestionEntity.class))).thenReturn(List.of());
        when(mongoTemplate.count(any(Query.class), any(Class.class))).thenReturn(1L);
        when(questionnaireQuestionEntityMapper.toConfiguredQuestions(eq(List.of()), any(Map.class))).thenReturn(List.of());
        when(questionnaireEntityMapper.toDomain(eq(entity), any(List.class))).thenReturn(questionnaire);

        var criteria = new SearchQuestionnaireByFilter(
                null,
                null,
                null,
                null,
                null,
                null,
                HybridPageRequest.ofPage(0, 10, List.of(new SortSpec("id", SortDirection.ASC)))
        );

        var page = adapter.findAll(criteria);

        assertEquals(PageMode.PAGE, page.mode());
        assertEquals(1, page.content().size());
        assertEquals("qn_query_page_1", page.content().getFirst().questionnaireId().id());
        assertEquals(1L, page.totalElements());
    }

    @Test
    @DisplayName("should find all using cursor mode")
    void shouldFindAllUsingCursorMode() {
        var q1 = MongoTestDataFactory.newQuestionnaire("qn_query_cursor_1", "channel_1", "journey_1");
        var q2 = MongoTestDataFactory.newQuestionnaire("qn_query_cursor_2", "channel_1", "journey_1");
        var q3 = MongoTestDataFactory.newQuestionnaire("qn_query_cursor_3", "channel_1", "journey_1");
        var anchor = MongoTestDataFactory.newQuestionnaire("qn_query_cursor_0", "channel_1", "journey_1");

        var e1 = new QuestionnaireEntity("qn_query_cursor_1|channel_1|journey_1", q1.id(), q1.channelDistributionId(),
                q1.journeyDistributionId(), q1.description(), q1.status(), null, 0);
        var e2 = new QuestionnaireEntity("qn_query_cursor_2|channel_1|journey_1", q2.id(), q2.channelDistributionId(),
                q2.journeyDistributionId(), q2.description(), q2.status(), null, 0);
        var e3 = new QuestionnaireEntity("qn_query_cursor_3|channel_1|journey_1", q3.id(), q3.channelDistributionId(),
                q3.journeyDistributionId(), q3.description(), q3.status(), null, 0);
        var anchorEntity = new QuestionnaireEntity("qn_query_cursor_0|channel_1|journey_1", anchor.id(),
                anchor.channelDistributionId(), anchor.journeyDistributionId(), anchor.description(), anchor.status(), null, 0);

        when(mongoTemplate.find(any(Query.class), eq(QuestionnaireEntity.class))).thenReturn(List.of(e1, e2, e3));
        when(mongoTemplate.find(any(Query.class), eq(QuestionnaireQuestionEntity.class))).thenReturn(List.of());
        when(mongoTemplate.findOne(any(Query.class), eq(QuestionnaireEntity.class))).thenReturn(anchorEntity);
        when(questionnaireQuestionEntityMapper.toConfiguredQuestions(eq(List.of()), any(Map.class))).thenReturn(List.of());
        when(questionnaireEntityMapper.toDomain(eq(e1), any(List.class))).thenReturn(q1);
        when(questionnaireEntityMapper.toDomain(eq(e2), any(List.class))).thenReturn(q2);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);

        var criteria = new SearchQuestionnaireByFilter(
                null,
                null,
                null,
                null,
                null,
                null,
                HybridPageRequest.ofCursor("qn_query_cursor_0", 2, List.of())
        );

        var page = adapter.findAll(criteria);

        verify(mongoTemplate).find(queryCaptor.capture(), eq(QuestionnaireEntity.class));
        Document sortObject = queryCaptor.getValue().getSortObject();
        Document queryObject = queryCaptor.getValue().getQueryObject();

        assertEquals(PageMode.CURSOR, page.mode());
        assertEquals(2, page.content().size());
        assertTrue(page.hasNext());
        assertEquals("qn_query_cursor_2", page.nextCursor());
        assertEquals("id", page.appliedSort().getFirst().field());
        assertEquals(1, sortObject.get("id"));
        assertTrue(queryObject.toJson().contains("$or") || queryObject.toJson().contains("$gt"));
        assertTrue(queryObject.toJson().contains(anchor.id()));
    }
}

