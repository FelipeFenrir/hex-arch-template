package com.acme.orderquestionnaire.adapters.out.mongo.config;

import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.entity.QuestionnaireQuestionEntity;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

/**
 * Programmatic Mongo index definitions.
 *
 * <p>Declared indexes:
 * <ul>
 *   <li>{@code questionnaire_questions}: compound {@code (questionnaire_document_id, order)} for
 *       ordered retrieval of all questions belonging to a questionnaire.</li>
 *   <li>{@code questionnaire_questions}: single {@code question_id} for reverse-lookup
 *       (find questionnaires that reference a given question).</li>
 * </ul>
 */
@Configuration
public class MongoIndexConfiguration {

    private final MongoTemplate mongoTemplate;

    public MongoIndexConfiguration(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initIndexes() {
        IndexOperations ops = mongoTemplate.indexOps(QuestionnaireQuestionEntity.class);

        ops.createIndex(new CompoundIndexDefinition(
                new org.bson.Document("questionnaire_document_id", 1)
                        .append("order", 1)
        ));

        ops.createIndex(new Index()
                .on("question_id", Sort.Direction.ASC)
                .named("idx_questionnaire_questions_question_id"));
    }
}


