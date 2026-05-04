package com.acme.orderquestionnaire.adapters.out.mongo.unit.audit.mapper;

import com.acme.orderquestionnaire.adapters.out.mongo.audit.mapper.AuditInfoDocumentMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.audit.mapper.AuditUserDocumentMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.unit.support.MongoTestDataFactory;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@UnitTest
@DisplayName("AuditInfoDocumentMapper")
class AuditInfoDocumentMapperTest {

    private final AuditInfoDocumentMapper mapper = new AuditInfoDocumentMapper(new AuditUserDocumentMapper());

    // ── Mapping tests ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("should map created audit info to document")
    void shouldMapCreatedAuditInfoToDocument() {
        var auditInfo = MongoTestDataFactory.createdAuditInfo();

        var doc = mapper.toDocument(auditInfo);

        assertEquals(auditInfo.createdBy().id().stringfyId(), doc.createdBy().id());
        assertEquals(auditInfo.createdAt(), doc.createdAt());
        assertNull(doc.updatedBy());
        assertNull(doc.updatedAt());
    }

    @Test
    @DisplayName("should map updated audit info to document and back")
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

