package com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.mapper;

import com.acme.orderquestionnaire.adapters.out.mongo.audit.mapper.AuditInfoDocumentMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.audit.mapper.AuditUserDocumentMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.support.MongoTestDataFactory;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestionnaireEntityMapperTest {

    private final QuestionnaireEntityMapper mapper =
            new QuestionnaireEntityMapper(new AuditInfoDocumentMapper(new AuditUserDocumentMapper()));

    @Test
    void shouldMapDomainToEntityAndBack() {
        Questionnaire questionnaire = newQuestionnaire("questionnaire_mapper_1");

        var entity = mapper.toEntity(questionnaire);
        var mappedBack = mapper.toDomain(entity);

        assertEquals(questionnaire.id(), entity.id());
        assertEquals(questionnaire.channelDistributionId(), entity.channelDistributionId());
        assertEquals(questionnaire.journeyDistributionId(), entity.journeyDistributionId());
        assertEquals(questionnaire.description(), entity.description());

        assertEquals(questionnaire.questionnaireId(), mappedBack.questionnaireId());
        assertEquals(questionnaire.description(), mappedBack.description());
        assertEquals(questionnaire.status(), mappedBack.status());
    }

    @Test
    void shouldGenerateDeterministicDocumentId() {
        String documentId = mapper.toDocumentId("q1", "channelA", "journeyB");
        assertEquals("q1|channelA|journeyB", documentId);
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

