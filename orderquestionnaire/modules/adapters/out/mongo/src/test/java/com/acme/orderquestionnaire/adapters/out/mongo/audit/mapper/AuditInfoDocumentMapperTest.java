package com.acme.orderquestionnaire.adapters.out.mongo.audit.mapper;

import com.acme.orderquestionnaire.adapters.out.mongo.support.MongoTestDataFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuditInfoDocumentMapperTest {

    private final AuditInfoDocumentMapper mapper = new AuditInfoDocumentMapper(new AuditUserDocumentMapper());

    @Test
    void shouldMapCreatedAuditInfoToDocument() {
        var auditInfo = MongoTestDataFactory.createdAuditInfo();

        var doc = mapper.toDocument(auditInfo);

        assertEquals(auditInfo.createdBy().id().stringfyId(), doc.createdBy().id());
        assertEquals(auditInfo.createdAt(), doc.createdAt());
        assertNull(doc.updatedBy());
        assertNull(doc.updatedAt());
    }

    @Test
    void shouldMapUpdatedAuditInfoToDocumentAndBack() {
        var auditInfo = MongoTestDataFactory.updatedAuditInfo();

        var doc = mapper.toDocument(auditInfo);
        var mappedBack = mapper.toDomain(doc);

        assertEquals(auditInfo.createdBy().id().stringfyId(), mappedBack.createdBy().id().stringfyId());
        assertEquals(auditInfo.createdAt(), mappedBack.createdAt());
        assertEquals(auditInfo.updatedBy().id().stringfyId(), mappedBack.updatedBy().id().stringfyId());
        assertEquals(auditInfo.updatedAt(), mappedBack.updatedAt());
    }
}

