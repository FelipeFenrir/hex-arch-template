package com.acme.orderquestionnaire.adapters.out.mongo.question;

import com.acme.orderquestionnaire.adapters.out.mongo.question.entity.QuestionEntity;
import com.acme.orderquestionnaire.adapters.out.mongo.question.mapper.QuestionEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.question.repository.QuestionCommandRepository;
import com.acme.orderquestionnaire.adapters.out.mongo.support.MongoTestDataFactory;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionCommandAdapterTest {

    @Mock
    private QuestionCommandRepository repository;

    @Mock
    private QuestionEntityMapper mapper;

    @InjectMocks
    private QuestionCommandAdapter adapter;

    @Test
    void shouldCreateQuestionSuccessfully() {
        Question question = MongoTestDataFactory.newQuestion("question_create");
        QuestionEntity entity = new QuestionEntity("question_create", "Question", question.status(), "sales_item_code_1", null);

        when(mapper.toEntity(question)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(question);

        Result<Question, List<DomainError>> result = adapter.create(question);

        assertTrue(result instanceof Result.Success<Question, List<DomainError>>);
        assertEquals(question, ((Result.Success<Question, List<DomainError>>) result).value());
    }

    @Test
    void shouldUpdateQuestionSuccessfully() {
        Question question = MongoTestDataFactory.rehydratedActiveQuestion("question_update");
        QuestionEntity entity = new QuestionEntity("question_update", "Question", question.status(), "sales_item_code_2", null);

        when(mapper.toEntity(question)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(question);

        Result<Question, List<DomainError>> result = adapter.update(question);

        assertTrue(result instanceof Result.Success<Question, List<DomainError>>);
        assertEquals(question, ((Result.Success<Question, List<DomainError>>) result).value());
    }

    @Test
    void shouldReturnExistsById() {
        when(repository.existsById("question_exists")).thenReturn(true);

        boolean exists = adapter.existsById("question_exists");

        assertTrue(exists);
    }

    @Test
    void shouldFindQuestionByIdWhenExists() {
        Question question = MongoTestDataFactory.newQuestion("question_find");
        QuestionEntity entity = new QuestionEntity("question_find", "Question", question.status(), "sales_item_code_1", null);

        when(repository.findById("question_find")).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(question);

        Optional<Question> found = adapter.findQuestionById("question_find");

        assertTrue(found.isPresent());
        assertEquals(question, found.get());
    }

    @Test
    void shouldReturnEmptyWhenQuestionNotFound() {
        when(repository.findById("question_missing")).thenReturn(Optional.empty());

        Optional<Question> found = adapter.findQuestionById("question_missing");

        assertTrue(found.isEmpty());
    }

    @Test
    void shouldDeleteQuestionByIdSuccessfully() {
        Result<Void, List<DomainError>> result = adapter.deleteById("question_delete");

        verify(repository).deleteById("question_delete");
        assertTrue(result instanceof Result.Success<Void, List<DomainError>>);
    }

    @Test
    void shouldReturnFailureWhenDeleteByIdThrowsException() {
        doThrow(new RuntimeException("mongo down")).when(repository).deleteById("question_delete_fail");

        Result<Void, List<DomainError>> result = adapter.deleteById("question_delete_fail");

        assertTrue(result instanceof Result.Failure<Void, List<DomainError>>);
        var errors = ((Result.Failure<Void, List<DomainError>>) result).error();
        assertEquals(1, errors.size());
        assertEquals("QUESTION_DELETE_FAILED", errors.getFirst().code());
    }

    @Test
    void shouldDeleteAllByIdsSuccessfully() {
        List<String> ids = List.of("question_1", "question_2");

        Result<Integer, List<DomainError>> result = adapter.deleteAllByIds(ids);

        verify(repository).deleteAllById(ids);
        assertTrue(result instanceof Result.Success<Integer, List<DomainError>>);
        assertEquals(2, ((Result.Success<Integer, List<DomainError>>) result).value());
    }

    @Test
    void shouldReturnFailureWhenDeleteAllByIdsThrowsException() {
        List<String> ids = List.of("question_1", "question_2");
        doThrow(new RuntimeException("mongo down")).when(repository).deleteAllById(ids);

        Result<Integer, List<DomainError>> result = adapter.deleteAllByIds(ids);

        assertTrue(result instanceof Result.Failure<Integer, List<DomainError>>);
        var errors = ((Result.Failure<Integer, List<DomainError>>) result).error();
        assertEquals(1, errors.size());
        assertEquals("QUESTION_DELETE_FAILED", errors.getFirst().code());
        assertTrue(errors.getFirst().message().contains("bulk"));
    }
}


