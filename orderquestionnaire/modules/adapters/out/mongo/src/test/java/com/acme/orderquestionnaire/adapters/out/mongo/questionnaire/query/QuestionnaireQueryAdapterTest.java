package com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.query;

import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.QuestionnaireQueryAdapter;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.entity.QuestionnaireEntity;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.mapper.QuestionnaireEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.repository.QuestionnaireQueryRepository;
import com.acme.orderquestionnaire.adapters.out.mongo.support.MongoTestDataFactory;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.GetQuestionnaireById;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.SearchQuestionnaireByFilter;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageMode;
import com.acme.shared.engine.pagination.SortDirection;
import com.acme.shared.engine.pagination.SortSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionnaireQueryAdapterTest {

    @Mock
    private QuestionnaireQueryRepository questionnaireQueryRepository;

    @Mock
    private QuestionnaireEntityMapper questionnaireEntityMapper;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private QuestionnaireQueryAdapter adapter;

    @Test
    void shouldFindById() {
        var questionnaire = MongoTestDataFactory.newQuestionnaire("qn_query_find", "channel_1", "journey_1");
        var entity = new QuestionnaireEntity(
                "qn_query_find|channel_1|journey_1",
                questionnaire.id(),
                questionnaire.channelDistributionId(),
                questionnaire.journeyDistributionId(),
                questionnaire.description(),
                questionnaire.status(),
                questionnaire.configuredQuestions(),
                null
        );

        when(questionnaireEntityMapper.toDocumentId("qn_query_find", "channel_1", "journey_1"))
                .thenReturn("qn_query_find|channel_1|journey_1");
        when(questionnaireQueryRepository.findById("qn_query_find|channel_1|journey_1")).thenReturn(Optional.of(entity));
        when(questionnaireEntityMapper.toDomain(entity)).thenReturn(questionnaire);

        var found = adapter.findById(new GetQuestionnaireById(QuestionnaireId.of("qn_query_find", "channel_1", "journey_1")));

        assertTrue(found.isPresent());
        assertEquals("qn_query_find", found.get().questionnaireId().id());
    }

    @Test
    void shouldReturnEmptyWhenIdIsInvalid() {
        var found = adapter.findById(new GetQuestionnaireById(QuestionnaireId.of(" ", "channel_1", "journey_1")));
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldFindAllUsingPageMode() {
        var questionnaire = MongoTestDataFactory.newQuestionnaire("qn_query_page_1", "channel_1", "journey_1");
        var entity = new QuestionnaireEntity(
                "qn_query_page_1|channel_1|journey_1",
                questionnaire.id(),
                questionnaire.channelDistributionId(),
                questionnaire.journeyDistributionId(),
                questionnaire.description(),
                questionnaire.status(),
                questionnaire.configuredQuestions(),
                null
        );

        when(mongoTemplate.find(any(Query.class), eq(QuestionnaireEntity.class))).thenReturn(List.of(entity));
        when(mongoTemplate.count(any(Query.class), any(Class.class))).thenReturn(1L);
        when(questionnaireEntityMapper.toDomain(entity)).thenReturn(questionnaire);

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
    void shouldFindAllUsingCursorMode() {
        var q1 = MongoTestDataFactory.newQuestionnaire("qn_query_cursor_1", "channel_1", "journey_1");
        var q2 = MongoTestDataFactory.newQuestionnaire("qn_query_cursor_2", "channel_1", "journey_1");
        var q3 = MongoTestDataFactory.newQuestionnaire("qn_query_cursor_3", "channel_1", "journey_1");

        var e1 = new QuestionnaireEntity("qn_query_cursor_1|channel_1|journey_1", q1.id(), q1.channelDistributionId(),
                q1.journeyDistributionId(), q1.description(), q1.status(), q1.configuredQuestions(), null);
        var e2 = new QuestionnaireEntity("qn_query_cursor_2|channel_1|journey_1", q2.id(), q2.channelDistributionId(),
                q2.journeyDistributionId(), q2.description(), q2.status(), q2.configuredQuestions(), null);
        var e3 = new QuestionnaireEntity("qn_query_cursor_3|channel_1|journey_1", q3.id(), q3.channelDistributionId(),
                q3.journeyDistributionId(), q3.description(), q3.status(), q3.configuredQuestions(), null);

        when(mongoTemplate.find(any(Query.class), eq(QuestionnaireEntity.class))).thenReturn(List.of(e1, e2, e3));
        when(questionnaireEntityMapper.toDomain(e1)).thenReturn(q1);
        when(questionnaireEntityMapper.toDomain(e2)).thenReturn(q2);

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

        assertEquals(PageMode.CURSOR, page.mode());
        assertEquals(2, page.content().size());
        assertTrue(page.hasNext());
        assertEquals("qn_query_cursor_2", page.nextCursor());
        assertEquals("id", page.appliedSort().getFirst().field());
    }
}

