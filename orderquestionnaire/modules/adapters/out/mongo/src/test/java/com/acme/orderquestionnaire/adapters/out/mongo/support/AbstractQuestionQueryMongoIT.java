package com.acme.orderquestionnaire.adapters.out.mongo.support;

import com.acme.orderquestionnaire.adapters.out.mongo.audit.mapper.AuditInfoDocumentMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.audit.mapper.AuditUserDocumentMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.question.QuestionQueryAdapter;
import com.acme.orderquestionnaire.adapters.out.mongo.question.mapper.QuestionEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.question.repository.QuestionCommandRepository;
import org.junit.jupiter.api.BeforeEach;
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


@SpringBootTest(classes = AbstractQuestionQueryMongoIT.QueryMongoTestApplication.class)
public abstract class AbstractQuestionQueryMongoIT extends AbstractMongoContainerIT {

    @Autowired
    protected QuestionCommandRepository questionCommandRepository;

    @Autowired
    protected QuestionEntityMapper questionEntityMapper;

    @BeforeEach
    void cleanDatabase() {
        questionCommandRepository.deleteAll();
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
            QuestionQueryAdapter.class
    })
    static class QueryMongoTestApplication {
    }
}
