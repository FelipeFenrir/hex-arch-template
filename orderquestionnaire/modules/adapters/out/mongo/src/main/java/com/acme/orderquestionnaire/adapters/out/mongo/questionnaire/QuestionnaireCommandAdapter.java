package com.acme.orderquestionnaire.adapters.out.mongo.questionnaire;

import com.acme.orderquestionnaire.adapters.out.mongo.question.entity.QuestionEntity;
import com.acme.orderquestionnaire.adapters.out.mongo.question.mapper.QuestionEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.entity.QuestionnaireEntity;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.entity.QuestionnaireQuestionEntity;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.mapper.QuestionnaireEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.mapper.QuestionnaireQuestionEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.repository.QuestionnaireCommandRepository;
import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.observability.Loggable;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireCommandOutPort;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.pattern.result.DomainError;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.adapter.OutputAdapter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@OutputAdapter
@Loggable
public class QuestionnaireCommandAdapter implements QuestionnaireCommandOutPort {

    private final QuestionnaireCommandRepository questionnaireCommandRepository;
    private final QuestionnaireEntityMapper questionnaireEntityMapper;
    private final QuestionnaireQuestionEntityMapper questionnaireQuestionEntityMapper;
    private final QuestionEntityMapper questionEntityMapper;
    private final MongoTemplate mongoTemplate;

    public QuestionnaireCommandAdapter(QuestionnaireCommandRepository questionnaireCommandRepository,
                                       QuestionnaireEntityMapper questionnaireEntityMapper,
                                       QuestionnaireQuestionEntityMapper questionnaireQuestionEntityMapper,
                                       QuestionEntityMapper questionEntityMapper,
                                       MongoTemplate mongoTemplate) {
        this.questionnaireCommandRepository = Objects.requireNonNull(questionnaireCommandRepository,
                "questionnaireCommandRepository must not be null");
        this.questionnaireEntityMapper = Objects.requireNonNull(questionnaireEntityMapper,
                "questionnaireEntityMapper must not be null");
        this.questionnaireQuestionEntityMapper = Objects.requireNonNull(questionnaireQuestionEntityMapper,
                "questionnaireQuestionEntityMapper must not be null");
        this.questionEntityMapper = Objects.requireNonNull(questionEntityMapper,
                "questionEntityMapper must not be null");
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate,
                "mongoTemplate must not be null");
    }

    @Override
    public Result<Questionnaire, List<DomainError>> create(Questionnaire entry) {
        QuestionnaireEntity saved = questionnaireCommandRepository.save(questionnaireEntityMapper.toEntity(entry));
        persistQuestions(entry);
        replaceQuestionnaireQuestions(saved.documentId(), entry);
        return Result.success(entry);
    }

    @Override
    public Result<Questionnaire, List<DomainError>> update(Questionnaire questionnaire) {
        QuestionnaireEntity saved = questionnaireCommandRepository.save(questionnaireEntityMapper.toEntity(questionnaire));
        persistQuestions(questionnaire);
        replaceQuestionnaireQuestions(saved.documentId(), questionnaire);
        return Result.success(questionnaire);
    }

    @Override
    public boolean existsById(QuestionnaireId id) {
        return questionnaireCommandRepository.existsById(toDocumentId(id));
    }

    @Override
    public Optional<Questionnaire> findQuestionnaireById(QuestionnaireId id) {
        return questionnaireCommandRepository.findById(toDocumentId(id))
                .map(this::toDomainAggregate);
    }

    @Override
    public Result<Void, List<DomainError>> deleteById(QuestionnaireId id) {
        try {
            String documentId = toDocumentId(id);
            deleteQuestionnaireQuestions(documentId);
            questionnaireCommandRepository.deleteById(documentId);
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

        Query query = new Query(Criteria.where("question_id").in(questionIds));
        List<QuestionnaireQuestionEntity> entities = mongoTemplate.find(query, QuestionnaireQuestionEntity.class);

        Map<String, LinkedHashSet<String>> temp = new LinkedHashMap<>();
        for (QuestionnaireQuestionEntity entity : entities) {
            temp.computeIfAbsent(entity.questionId(), ignored -> new LinkedHashSet<>())
                    .add(entity.questionnaireId());
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

    private void persistQuestions(Questionnaire questionnaire) {
        questionnaire.configuredQuestions().stream()
                .map(ConfiguredQuestion::question)
                .map(questionEntityMapper::toEntity)
                .forEach(questionEntity -> mongoTemplate.save(questionEntity));
    }

    private void replaceQuestionnaireQuestions(String questionnaireDocumentId, Questionnaire questionnaire) {
        deleteQuestionnaireQuestions(questionnaireDocumentId);

        questionnaire.configuredQuestions().stream()
                .map(configuredQuestion -> questionnaireQuestionEntityMapper
                        .toEntity(questionnaireDocumentId, questionnaire, configuredQuestion))
                .forEach(mongoTemplate::save);

        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(questionnaireDocumentId)),
                new Update().set("questions_count", questionnaire.configuredQuestions().size()),
                QuestionnaireEntity.class
        );
    }

    private void deleteQuestionnaireQuestions(String questionnaireDocumentId) {
        mongoTemplate.remove(
                Query.query(Criteria.where("questionnaire_document_id").is(questionnaireDocumentId)),
                QuestionnaireQuestionEntity.class
        );
    }

    private Questionnaire toDomainAggregate(QuestionnaireEntity questionnaireEntity) {
        List<QuestionnaireQuestionEntity> links = mongoTemplate.find(
                Query.query(Criteria.where("questionnaire_document_id").is(questionnaireEntity.documentId())),
                QuestionnaireQuestionEntity.class
        );

        List<String> questionIds = links.stream()
                .map(QuestionnaireQuestionEntity::questionId)
                .distinct()
                .toList();

        Map<String, Question> questionsById = loadQuestionsByIds(questionIds);

        List<ConfiguredQuestion> configuredQuestions = questionnaireQuestionEntityMapper
                .toConfiguredQuestions(links, questionsById);

        return questionnaireEntityMapper.toDomain(questionnaireEntity, configuredQuestions);
    }

    private Map<String, Question> loadQuestionsByIds(List<String> questionIds) {
        if (questionIds.isEmpty()) {
            return Map.of();
        }

        Query questionQuery = Query.query(Criteria.where("_id").in(questionIds));
        List<QuestionEntity> questionEntities = mongoTemplate.find(questionQuery, QuestionEntity.class);

        return questionEntities.stream()
                .map(questionEntityMapper::toDomain)
                .collect(Collectors.toMap(
                        Question::id,
                        question -> question,
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
    }
}

