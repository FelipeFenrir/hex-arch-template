package com.acme.orderquestionnaire.adapters.out.mongo.support;

import com.acme.orderquestionnaire.adapters.out.mongo.audit.mapper.AuditInfoDocumentMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.audit.mapper.AuditUserDocumentMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.question.mapper.QuestionEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.question.repository.QuestionCommandRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Base para futuros testes de integração do Query Adapter com Mongo + Testcontainers.
 *
 * Reaproveita o container definido em {@link AbstractMongoContainerIT} e provê um
 * contexto mínimo de Spring Data Mongo para o domínio de question.
 */
@SpringBootTest(classes = AbstractQuestionQueryMongoIT.QueryMongoTestApplication.class)
public abstract class AbstractQuestionQueryMongoIT extends AbstractMongoContainerIT {

    @Autowired
    protected QuestionCommandRepository questionCommandRepository;

    @BeforeEach
    void cleanDatabase() {
        questionCommandRepository.deleteAll();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableMongoRepositories(basePackageClasses = QuestionCommandRepository.class)
    @Import({
            AuditUserDocumentMapper.class,
            AuditInfoDocumentMapper.class,
            QuestionEntityMapper.class
    })
    static class QueryMongoTestApplication {
    }
}

