package com.acme.orderquestionnaire.adapters.out.mongo.question.mapper;

import com.acme.orderquestionnaire.adapters.out.mongo.audit.mapper.AuditInfoDocumentMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.audit.mapper.AuditUserDocumentMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.question.entity.QuestionEntity;
import com.acme.orderquestionnaire.adapters.out.mongo.support.MongoTestDataFactory;
import com.acme.shared.enumerator.ParameterizationStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuestionEntityMapperTest {

    private final QuestionEntityMapper mapper =
            new QuestionEntityMapper(new AuditInfoDocumentMapper(new AuditUserDocumentMapper()));

    @Test
    void shouldMapDomainToEntityAndBack() {
        var question = MongoTestDataFactory.rehydratedActiveQuestion("question_mapper_roundtrip");

        var entity = mapper.toEntity(question);
        var mappedBack = mapper.toDomain(entity);

        assertEquals(question.id(), entity.id());
        assertEquals(question.label(), entity.label());
        assertEquals(question.status(), entity.status());
        assertEquals(question.salesItemReferenceCode(), entity.salesItemReferenceCode());

        assertEquals(question.id(), mappedBack.id());
        assertEquals(question.label(), mappedBack.label());
        assertEquals(question.status(), mappedBack.status());
        assertEquals(question.salesItemReferenceCode(), mappedBack.salesItemReferenceCode());
        assertEquals(question.auditInfo().createdBy().id().stringfyId(), mappedBack.auditInfo().createdBy().id().stringfyId());
        assertEquals(question.auditInfo().updatedBy().id().stringfyId(), mappedBack.auditInfo().updatedBy().id().stringfyId());
    }

    @Test
    void shouldThrowWhenEntityCannotBeRehydrated() {
        var auditInfoDoc = new AuditInfoDocumentMapper(new AuditUserDocumentMapper())
                .toDocument(MongoTestDataFactory.createdAuditInfo());

        var invalidEntity = new QuestionEntity(
                "question_invalid",
                null,
                ParameterizationStatus.ACTIVE,
                "sales_item_code_1",
                auditInfoDoc
        );

        assertThrows(IllegalStateException.class, () -> mapper.toDomain(invalidEntity));
    }
}

