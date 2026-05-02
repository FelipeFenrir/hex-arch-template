package com.acme.orderquestionnaire.adapters.out.mongo.unit.questionnaire.command;

import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.QuestionnaireCommandAdapter;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.entity.QuestionnaireEntity;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.entity.QuestionnaireQuestionEntity;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.mapper.QuestionnaireEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.mapper.QuestionnaireQuestionEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.repository.QuestionnaireCommandRepository;
import com.acme.orderquestionnaire.adapters.out.mongo.question.mapper.QuestionEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.unit.support.MongoTestDataFactory;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireFactory;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("QuestionnaireCommandAdapter")
class QuestionnaireCommandAdapterTest {

    @Mock
    private QuestionnaireCommandRepository repository;

    @Mock
    private QuestionnaireEntityMapper mapper;

    @Mock
    private QuestionnaireQuestionEntityMapper questionnaireQuestionEntityMapper;

    @Mock
    private QuestionEntityMapper questionEntityMapper;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private QuestionnaireCommandAdapter adapter;

    // ── Create operations ─────────────────────────────────────────────────────

    @Test
    @DisplayName("should create questionnaire successfully")
    void shouldCreateQuestionnaireSuccessfully() {
        Questionnaire questionnaire = newQuestionnaire("qn_create");
        QuestionnaireEntity entity = new QuestionnaireEntity("doc", "qn_create", "channel_1", "journey_1",
                questionnaire.description(), questionnaire.status(), null, 0);

        when(mapper.toEntity(questionnaire)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);

        Result<Questionnaire, List<DomainError>> result = adapter.create(questionnaire);

        assertTrue(result instanceof Result.Success<Questionnaire, List<DomainError>>);
        assertEquals("qn_create", ((Result.Success<Questionnaire, List<DomainError>>) result).value().id());
    }

    @Test
    @DisplayName("should update questionnaire successfully")
    void shouldUpdateQuestionnaireSuccessfully() {
        Questionnaire questionnaire = newQuestionnaire("qn_update");
        QuestionnaireEntity entity = new QuestionnaireEntity("doc", "qn_update", "channel_1", "journey_1",
                questionnaire.description(), questionnaire.status(), null, 0);

        when(mapper.toEntity(questionnaire)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);

        Result<Questionnaire, List<DomainError>> result = adapter.update(questionnaire);

        assertTrue(result instanceof Result.Success<Questionnaire, List<DomainError>>);
        assertEquals("qn_update", ((Result.Success<Questionnaire, List<DomainError>>) result).value().id());
    }

    // ── Read operations ──────────────────────────────────────────────────────

    @Test
    @DisplayName("should exists by id")
    void shouldExistsById() {
        QuestionnaireId id = QuestionnaireId.of("qn_1", "channel_1", "journey_1");
        when(mapper.toDocumentId("qn_1", "channel_1", "journey_1")).thenReturn("qn_1|channel_1|journey_1");
        when(repository.existsById("qn_1|channel_1|journey_1")).thenReturn(true);

        boolean exists = adapter.existsById(id);

        assertTrue(exists);
    }

    @Test
    @DisplayName("should find by id")
    void shouldFindById() {
        Questionnaire questionnaire = newQuestionnaire("qn_find");
        QuestionnaireEntity entity = new QuestionnaireEntity("doc", "qn_find", "channel_1", "journey_1",
                questionnaire.description(), questionnaire.status(), null, 0);

        when(mapper.toDocumentId("qn_find", "channel_1", "journey_1")).thenReturn("qn_find|channel_1|journey_1");
        when(repository.findById("qn_find|channel_1|journey_1")).thenReturn(Optional.of(entity));
        when(mongoTemplate.find(any(Query.class), eq(QuestionnaireQuestionEntity.class))).thenReturn(List.of());
        when(questionnaireQuestionEntityMapper.toConfiguredQuestions(eq(List.of()), any(Map.class))).thenReturn(List.of());
        when(mapper.toDomain(eq(entity), any(List.class))).thenReturn(questionnaire);

        Optional<Questionnaire> found = adapter.findQuestionnaireById(QuestionnaireId.of("qn_find", "channel_1", "journey_1"));

        assertTrue(found.isPresent());
        assertEquals("qn_find", found.get().id());
    }

    // ── Delete operations ────────────────────────────────────────────────────

    @Test
    @DisplayName("should delete by id successfully")
    void shouldDeleteByIdSuccessfully() {
        QuestionnaireId id = QuestionnaireId.of("qn_delete", "channel_1", "journey_1");
        when(mapper.toDocumentId("qn_delete", "channel_1", "journey_1")).thenReturn("qn_delete|channel_1|journey_1");

        Result<Void, List<DomainError>> result = adapter.deleteById(id);

        verify(repository).deleteById("qn_delete|channel_1|journey_1");
        assertTrue(result instanceof Result.Success<Void, List<DomainError>>);
    }

    @Test
    @DisplayName("should return failure when delete throws")
    void shouldReturnFailureWhenDeleteThrows() {
        QuestionnaireId id = QuestionnaireId.of("qn_delete_fail", "channel_1", "journey_1");
        when(mapper.toDocumentId("qn_delete_fail", "channel_1", "journey_1"))
                .thenReturn("qn_delete_fail|channel_1|journey_1");
        doThrow(new RuntimeException("mongo down")).when(repository).deleteById("qn_delete_fail|channel_1|journey_1");

        Result<Void, List<DomainError>> result = adapter.deleteById(id);

        assertTrue(result instanceof Result.Failure<Void, List<DomainError>>);
    }

    // ── Query operations ─────────────────────────────────────────────────────

    @Test
    @DisplayName("should find referencing questionnaire ids by question ids")
    void shouldFindReferencingQuestionnaireIdsByQuestionIds() {
        QuestionnaireQuestionEntity entity = new QuestionnaireQuestionEntity(
                "doc|question_1",
                "doc",
                "questionnaire_1",
                "channel_1",
                "journey_1",
                "question_1",
                null,
                null,
                0
        );

        when(mongoTemplate.find(any(Query.class), eq(QuestionnaireQuestionEntity.class))).thenReturn(List.of(entity));

        Map<String, List<String>> references = adapter.findReferencingQuestionnaireIdsByQuestionIds(List.of("question_1"));

        assertEquals(1, references.size());
        assertEquals(List.of("questionnaire_1"), references.get("question_1"));
    }

    private Questionnaire newQuestionnaire(String id) {
        return QuestionnaireFactory.createNew(
                        id,
                        "channel_1",
                        "journey_1",
                        "Questionnaire " + id,
                        MongoTestDataFactory.createdAuditInfo())
                .flatMap(QuestionnaireFactory.QuestionnaireBuilder::build)
                .getOrElseThrow(errors -> new IllegalStateException("Invalid questionnaire fixture: " + errors));
    }
}

