package com.acme.orderquestionnaire.adapters.out.mongo.questionnaire;

import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.entity.QuestionnaireEntity;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.mapper.QuestionnaireEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.repository.QuestionnaireCommandRepository;
import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.adapter.OutputAdapter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@OutputAdapter
public class QuestionnaireCommandAdapter implements QuestionnaireCommandOutPort {

    private final QuestionnaireCommandRepository questionnaireCommandRepository;
    private final QuestionnaireEntityMapper questionnaireEntityMapper;
    private final MongoTemplate mongoTemplate;

    public QuestionnaireCommandAdapter(QuestionnaireCommandRepository questionnaireCommandRepository,
                                       QuestionnaireEntityMapper questionnaireEntityMapper,
                                       MongoTemplate mongoTemplate) {
        this.questionnaireCommandRepository = Objects.requireNonNull(questionnaireCommandRepository,
                "questionnaireCommandRepository must not be null");
        this.questionnaireEntityMapper = Objects.requireNonNull(questionnaireEntityMapper,
                "questionnaireEntityMapper must not be null");
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate,
                "mongoTemplate must not be null");
    }

    @Override
    public Result<Questionnaire, List<DomainError>> create(Questionnaire entry) {
        QuestionnaireEntity saved = questionnaireCommandRepository.save(questionnaireEntityMapper.toEntity(entry));
        return Result.success(questionnaireEntityMapper.toDomain(saved));
    }

    @Override
    public Result<Questionnaire, List<DomainError>> update(Questionnaire questionnaire) {
        QuestionnaireEntity saved = questionnaireCommandRepository.save(questionnaireEntityMapper.toEntity(questionnaire));
        return Result.success(questionnaireEntityMapper.toDomain(saved));
    }

    @Override
    public boolean existsById(QuestionnaireId id) {
        return questionnaireCommandRepository.existsById(toDocumentId(id));
    }

    @Override
    public Optional<Questionnaire> findQuestionnaireById(QuestionnaireId id) {
        return questionnaireCommandRepository.findById(toDocumentId(id))
                .map(questionnaireEntityMapper::toDomain);
    }

    @Override
    public Result<Void, List<DomainError>> deleteById(QuestionnaireId id) {
        try {
            questionnaireCommandRepository.deleteById(toDocumentId(id));
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure(List.of(
                    QuestionnaireErrors.QUESTIONNAIRE_DELETE_FAILED.toDomainError(id.id(), e.getMessage())));
        }
    }

    @Override
    public Map<String, List<String>> findReferencingQuestionnaireIdsByQuestionIds(List<String> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Map.of();
        }

        Query query = new Query(Criteria.where("configured_questions.question.id").in(questionIds));
        List<QuestionnaireEntity> entities = mongoTemplate.find(query, QuestionnaireEntity.class);

        Map<String, LinkedHashSet<String>> temp = new LinkedHashMap<>();
        for (QuestionnaireEntity entity : entities) {
            if (entity.configuredQuestions() == null) {
                continue;
            }

            for (var configuredQuestion : entity.configuredQuestions()) {
                String questionId = configuredQuestion.question().id();
                if (!questionIds.contains(questionId)) {
                    continue;
                }

                temp.computeIfAbsent(questionId, ignored -> new LinkedHashSet<>())
                        .add(entity.id());
            }
        }

        Map<String, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, LinkedHashSet<String>> entry : temp.entrySet()) {
            result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return result;
    }

    private String toDocumentId(QuestionnaireId id) {
        return questionnaireEntityMapper.toDocumentId(id.id(), id.channelDistributionId(), id.journeyDistributionId());
    }
}

