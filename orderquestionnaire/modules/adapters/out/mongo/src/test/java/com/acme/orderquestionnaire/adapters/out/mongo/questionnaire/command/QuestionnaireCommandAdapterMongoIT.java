package com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.command;

import com.acme.orderquestionnaire.adapters.out.mongo.audit.mapper.AuditInfoDocumentMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.audit.mapper.AuditUserDocumentMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.QuestionnaireCommandAdapter;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.mapper.QuestionnaireEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.repository.QuestionnaireCommandRepository;
import com.acme.orderquestionnaire.adapters.out.mongo.support.AbstractMongoContainerIT;
import com.acme.orderquestionnaire.adapters.out.mongo.support.MongoTestDataFactory;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.enumerator.ParameterizationStatus;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = QuestionnaireCommandAdapterMongoIT.TestApplication.class)
class QuestionnaireCommandAdapterMongoIT extends AbstractMongoContainerIT {

    private static final String CHANNEL = "channel_1";
    private static final String JOURNEY = "journey_1";

    @Autowired
    private QuestionnaireCommandAdapter adapter;

    @Autowired
    private QuestionnaireCommandRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateAndFindQuestionnaire() {
        Questionnaire questionnaire = MongoTestDataFactory.newQuestionnaire("qn_create_it", CHANNEL, JOURNEY);

        Result<Questionnaire, List<DomainError>> result = adapter.create(questionnaire);

        assertTrue(result instanceof Result.Success<Questionnaire, List<DomainError>>);
        assertEquals("qn_create_it", ((Result.Success<Questionnaire, List<DomainError>>) result).value().id());

        var found = adapter.findQuestionnaireById(QuestionnaireId.of("qn_create_it", CHANNEL, JOURNEY));
        assertTrue(found.isPresent());
        assertEquals("qn_create_it", found.get().id());
        assertEquals(ParameterizationStatus.DRAFT, found.get().status());
    }

    @Test
    void shouldUpdateQuestionnaireAndPersistNewState() {
        adapter.create(MongoTestDataFactory.newQuestionnaire("qn_update_it", CHANNEL, JOURNEY));

        Questionnaire updated = MongoTestDataFactory.rehydratedActiveQuestionnaire("qn_update_it", CHANNEL, JOURNEY);
        Result<Questionnaire, List<DomainError>> result = adapter.update(updated);

        assertTrue(result instanceof Result.Success<Questionnaire, List<DomainError>>);

        var found = adapter.findQuestionnaireById(QuestionnaireId.of("qn_update_it", CHANNEL, JOURNEY));
        assertTrue(found.isPresent());
        assertEquals(ParameterizationStatus.ACTIVE, found.get().status());
    }

    @Test
    void shouldReturnExistsById() {
        adapter.create(MongoTestDataFactory.newQuestionnaire("qn_exists_it", CHANNEL, JOURNEY));

        assertTrue(adapter.existsById(QuestionnaireId.of("qn_exists_it", CHANNEL, JOURNEY)));
    }

    @Test
    void shouldReturnFalseWhenNotExists() {
        assertFalse(adapter.existsById(QuestionnaireId.of("qn_missing_it", CHANNEL, JOURNEY)));
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        var found = adapter.findQuestionnaireById(QuestionnaireId.of("qn_not_found_it", CHANNEL, JOURNEY));

        assertTrue(found.isEmpty());
    }

    @Test
    void shouldDeleteById() {
        adapter.create(MongoTestDataFactory.newQuestionnaire("qn_delete_it", CHANNEL, JOURNEY));

        Result<Void, List<DomainError>> result = adapter.deleteById(QuestionnaireId.of("qn_delete_it", CHANNEL, JOURNEY));

        assertTrue(result instanceof Result.Success<Void, List<DomainError>>);
        assertTrue(adapter.findQuestionnaireById(QuestionnaireId.of("qn_delete_it", CHANNEL, JOURNEY)).isEmpty());
    }

    @Test
    void shouldReturnEmptyMapWhenNoReferences() {
        adapter.create(MongoTestDataFactory.newQuestionnaire("qn_noref_it", CHANNEL, JOURNEY));

        Map<String, List<String>> refs = adapter.findReferencingQuestionnaireIdsByQuestionIds(List.of("ghost_question"));

        assertTrue(refs.isEmpty());
    }

    @Test
    void shouldReturnEmptyMapWhenQuestionIdsListIsEmpty() {
        Map<String, List<String>> refs = adapter.findReferencingQuestionnaireIdsByQuestionIds(List.of());

        assertTrue(refs.isEmpty());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            JpaRepositoriesAutoConfiguration.class
    })
    @EnableMongoRepositories(basePackageClasses = QuestionnaireCommandRepository.class)
    @Import({
            AuditUserDocumentMapper.class,
            AuditInfoDocumentMapper.class,
            QuestionnaireEntityMapper.class,
            QuestionnaireCommandAdapter.class
    })
    static class TestApplication {
    }
}

