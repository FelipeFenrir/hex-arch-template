package com.acme.orderquestionnaire.adapters.out.mongo.unit.question.query;

import com.acme.orderquestionnaire.adapters.out.mongo.question.QuestionQueryAdapter;
import com.acme.orderquestionnaire.adapters.out.mongo.question.entity.QuestionEntity;
import com.acme.orderquestionnaire.adapters.out.mongo.question.mapper.QuestionEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.question.repository.QuestionQueryRepository;
import com.acme.orderquestionnaire.adapters.out.mongo.unit.support.MongoTestDataFactory;
import com.acme.orderquestionnaire.application.question.dto.queries.GetQuestionById;
import com.acme.orderquestionnaire.application.question.dto.queries.SearchQuestionByFilter;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("QuestionQueryAdapter")
class QuestionQueryAdapterTest {

    @Mock
    private QuestionQueryRepository questionQueryRepository;

    @Mock
    private QuestionEntityMapper questionEntityMapper;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private QuestionQueryAdapter adapter;

    // ── Read operations by id ─────────────────────────────���───────────────────

    @Test
    @DisplayName("should find by id")
    void shouldFindById() {
        var question = MongoTestDataFactory.newQuestion("question_query_find");
        var entity = new QuestionEntity("question_query_find", question.label(), question.status(), question.salesItemReferenceCode(), null);

        when(questionQueryRepository.findById("question_query_find")).thenReturn(Optional.of(entity));
        when(questionEntityMapper.toDomain(entity)).thenReturn(question);

        var found = adapter.findById(new GetQuestionById("question_query_find"));

        assertTrue(found.isPresent());
        assertEquals("question_query_find", found.get().id());
    }

    @Test
    void shouldReturnEmptyWhenIdIsInvalid() {
        var found = adapter.findById(new GetQuestionById(" "));
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldFindAllUsingPageMode() {
        var q1 = MongoTestDataFactory.newQuestion("question_query_page_1");
        var e1 = new QuestionEntity(q1.id(), q1.label(), q1.status(), q1.salesItemReferenceCode(), null);

        when(mongoTemplate.find(any(Query.class), eq(QuestionEntity.class))).thenReturn(List.of(e1));
        when(mongoTemplate.count(any(Query.class), any(Class.class))).thenReturn(1L);
        when(questionEntityMapper.toDomain(e1)).thenReturn(q1);

        var criteria = new SearchQuestionByFilter(
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
        assertEquals("question_query_page_1", page.content().getFirst().id());
        assertEquals(1L, page.totalElements());
    }

    @Test
    void shouldFindAllUsingCursorMode() {
        var q1 = MongoTestDataFactory.newQuestion("question_query_cursor_1");
        var q2 = MongoTestDataFactory.newQuestion("question_query_cursor_2");
        var q3 = MongoTestDataFactory.newQuestion("question_query_cursor_3");
        var anchor = MongoTestDataFactory.newQuestion("question_query_cursor_0");

        var e1 = new QuestionEntity(q1.id(), q1.label(), q1.status(), q1.salesItemReferenceCode(), null);
        var e2 = new QuestionEntity(q2.id(), q2.label(), q2.status(), q2.salesItemReferenceCode(), null);
        var e3 = new QuestionEntity(q3.id(), q3.label(), q3.status(), q3.salesItemReferenceCode(), null);
        var anchorEntity = new QuestionEntity(anchor.id(), anchor.label(), anchor.status(), anchor.salesItemReferenceCode(), null);

        when(mongoTemplate.find(any(Query.class), eq(QuestionEntity.class))).thenReturn(List.of(e1, e2, e3));
        when(questionQueryRepository.findById(anchor.id())).thenReturn(Optional.of(anchorEntity));
        when(questionEntityMapper.toDomain(e1)).thenReturn(q1);
        when(questionEntityMapper.toDomain(e2)).thenReturn(q2);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);

        var criteria = new SearchQuestionByFilter(
                null,
                null,
                null,
                null,
                null,
                HybridPageRequest.ofCursor(anchor.id(), 2, List.of(new SortSpec("label", SortDirection.DESC)))
        );

        var page = adapter.findAll(criteria);

        verify(mongoTemplate).find(queryCaptor.capture(), eq(QuestionEntity.class));
        Document sortObject = queryCaptor.getValue().getSortObject();
        Document queryObject = queryCaptor.getValue().getQueryObject();

        assertEquals(PageMode.CURSOR, page.mode());
        assertEquals(2, page.content().size());
        assertTrue(page.hasNext());
        assertEquals("question_query_cursor_2", page.nextCursor());
        assertEquals("label", page.appliedSort().getFirst().field());
        assertEquals(SortDirection.DESC, page.appliedSort().getFirst().direction());
        assertEquals("id", page.appliedSort().get(1).field());
        assertEquals(-1, sortObject.get("label"));
        assertEquals(1, sortObject.get("id"));
        assertTrue(queryObject.toJson().contains("$or"));
        assertTrue(queryObject.toJson().contains(anchor.label()));
    }
}


