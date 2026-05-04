package com.acme.orderquestionnaire.adapters.out.mongo.integration.question.command;

import com.acme.orderquestionnaire.adapters.out.mongo.audit.mapper.AuditInfoDocumentMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.audit.mapper.AuditUserDocumentMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.integration.support.AbstractMongoContainerIT;
import com.acme.orderquestionnaire.adapters.out.mongo.question.QuestionCommandAdapter;
import com.acme.orderquestionnaire.adapters.out.mongo.question.mapper.QuestionEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.question.repository.QuestionCommandRepository;
import com.acme.orderquestionnaire.adapters.out.mongo.unit.support.MongoTestDataFactory;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = QuestionCommandAdapterMongoIT.TestApplication.class)
@IntegrationTest
@DisplayName("QuestionCommandAdapter Mongo Integration Test")
class QuestionCommandAdapterMongoIT extends AbstractMongoContainerIT {

    @Autowired
    private QuestionCommandAdapter adapter;

    @Autowired
    private QuestionCommandRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateAndFindQuestion() {
        var question = MongoTestDataFactory.newQuestion("question_create_it");

        Result<?, List<DomainError>> created = adapter.create(question);

        assertTrue(created instanceof Result.Success<?, List<DomainError>>);
        var found = adapter.findQuestionById("question_create_it");
        assertTrue(found.isPresent());
        assertEquals("question_create_it", found.get().id());
        assertEquals(ParameterizationStatus.DRAFT, found.get().status());
    }

    @Test
    void shouldUpdateQuestionAndPersistNewState() {
        adapter.create(MongoTestDataFactory.newQuestion("question_update_it"));

        var updatedQuestion = MongoTestDataFactory.rehydratedActiveQuestion("question_update_it");
        Result<?, List<DomainError>> updated = adapter.update(updatedQuestion);

        assertTrue(updated instanceof Result.Success<?, List<DomainError>>);
        var found = adapter.findQuestionById("question_update_it");
        assertTrue(found.isPresent());
        assertEquals(ParameterizationStatus.ACTIVE, found.get().status());
        assertEquals("sales_item_code_2", found.get().salesItemReferenceCode());
    }

    @Test
    void shouldReturnExistsById() {
        adapter.create(MongoTestDataFactory.newQuestion("question_exists_it"));

        assertTrue(adapter.existsById("question_exists_it"));
    }

    @Test
    void shouldDeleteById() {
        adapter.create(MongoTestDataFactory.newQuestion("question_delete_it"));

        Result<Void, List<DomainError>> deleted = adapter.deleteById("question_delete_it");

        assertTrue(deleted instanceof Result.Success<Void, List<DomainError>>);
        assertTrue(adapter.findQuestionById("question_delete_it").isEmpty());
    }

    @Test
    void shouldDeleteAllByIds() {
        adapter.create(MongoTestDataFactory.newQuestion("question_bulk_1"));
        adapter.create(MongoTestDataFactory.newQuestion("question_bulk_2"));

        Result<Integer, List<DomainError>> deleted = adapter.deleteAllByIds(List.of("question_bulk_1", "question_bulk_2"));

        assertTrue(deleted instanceof Result.Success<Integer, List<DomainError>>);
        assertEquals(2, ((Result.Success<Integer, List<DomainError>>) deleted).value());
        assertTrue(repository.findAll().isEmpty());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            JpaRepositoriesAutoConfiguration.class
    })
    @EnableMongoRepositories(basePackageClasses = QuestionCommandRepository.class)
    @Import({
            AuditUserDocumentMapper.class,
            AuditInfoDocumentMapper.class,
            QuestionEntityMapper.class,
            QuestionCommandAdapter.class
    })
    static class TestApplication {
    }
}




