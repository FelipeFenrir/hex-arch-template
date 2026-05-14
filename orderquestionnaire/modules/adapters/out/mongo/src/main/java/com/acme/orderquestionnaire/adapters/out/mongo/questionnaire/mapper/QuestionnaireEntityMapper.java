package com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.mapper;

import com.acme.orderquestionnaire.adapters.out.mongo.audit.mapper.AuditInfoDocumentMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.entity.QuestionnaireEntity;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.QuestionnaireFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class QuestionnaireEntityMapper {

    private final AuditInfoDocumentMapper auditInfoDocumentMapper;

    public QuestionnaireEntityMapper(AuditInfoDocumentMapper auditInfoDocumentMapper) {
        this.auditInfoDocumentMapper = Objects.requireNonNull(auditInfoDocumentMapper,
                "auditInfoDocumentMapper must not be null");
    }

    public QuestionnaireEntity toEntity(Questionnaire questionnaire) {
        String documentId = toDocumentId(
                questionnaire.id(),
                questionnaire.channelDistributionId(),
                questionnaire.journeyDistributionId());

        return new QuestionnaireEntity(
                documentId,
                questionnaire.id(),
                questionnaire.channelDistributionId(),
                questionnaire.journeyDistributionId(),
                questionnaire.description(),
                questionnaire.status(),
                auditInfoDocumentMapper.toDocument(questionnaire.auditInfo()),
                questionnaire.configuredQuestions().size()
        );
    }

    public Questionnaire toDomain(QuestionnaireEntity entity) {
        return toDomain(entity, List.of());
    }

    public Questionnaire toDomain(QuestionnaireEntity entity, List<ConfiguredQuestion> configuredQuestions) {
        var auditInfo = auditInfoDocumentMapper.toDomain(entity.auditInfo());

        return QuestionnaireFactory
                .rehydrate(
                        entity.id(),
                        entity.channelDistributionId(),
                        entity.journeyDistributionId(),
                        entity.description(),
                        entity.status(),
                        auditInfo)
                .flatMap(builder -> builder.withQuestions(
                        configuredQuestions == null ? List.of() : configuredQuestions).build())
                .getOrElseThrow(errors -> new IllegalStateException(
                        "Failed to rehydrate Questionnaire [id=%s]: %s".formatted(entity.id(), errors)));
    }

    public String toDocumentId(String id, String channelDistributionId, String journeyDistributionId) {
        return id + "|" + channelDistributionId + "|" + journeyDistributionId;
    }
}

