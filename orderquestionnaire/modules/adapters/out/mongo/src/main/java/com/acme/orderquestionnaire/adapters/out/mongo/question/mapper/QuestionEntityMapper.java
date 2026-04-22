package com.acme.orderquestionnaire.adapters.out.mongo.question.mapper;

import com.acme.orderquestionnaire.adapters.out.mongo.audit.mapper.AuditInfoDocumentMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.question.entity.QuestionEntity;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.question.QuestionFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class QuestionEntityMapper {

    private final AuditInfoDocumentMapper auditInfoDocumentMapper;

    public QuestionEntityMapper(AuditInfoDocumentMapper auditInfoDocumentMapper) {
        this.auditInfoDocumentMapper = Objects.requireNonNull(auditInfoDocumentMapper,
                "auditInfoDocumentMapper must not be null");
    }

    public QuestionEntity toEntity(Question question) {
        return new QuestionEntity(
                question.id(),
                question.label(),
                question.status(),
                question.salesItemReferenceCode(),
                auditInfoDocumentMapper.toDocument(question.auditInfo())
        );
    }

    public Question toDomain(QuestionEntity entity) {
        var auditInfo = auditInfoDocumentMapper.toDomain(entity.auditInfo());

        return QuestionFactory
                .rehydrate(entity.id(), entity.label(), entity.status(), auditInfo)
                .flatMap(builder -> builder
                        .withSalesItemReferenceCode(entity.salesItemReferenceCode())
                        .build())
                .getOrElseThrow(errors -> new IllegalStateException(
                        "Failed to rehydrate Question [id=%s]: %s".formatted(entity.id(), errors)));
    }
}



