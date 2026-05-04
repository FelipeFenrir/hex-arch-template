package com.acme.orderquestionnaire.adapters.out.mongo.questionnaire;

import com.acme.orderquestionnaire.adapters.out.mongo.question.entity.QuestionEntity;
import com.acme.orderquestionnaire.adapters.out.mongo.question.mapper.QuestionEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.entity.QuestionnaireEntity;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.entity.QuestionnaireQuestionEntity;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.mapper.QuestionnaireEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.mapper.QuestionnaireQuestionEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.questionnaire.repository.QuestionnaireQueryRepository;
import com.acme.orderquestionnaire.adapters.out.mongo.support.MongoQuerySupport;
import com.acme.observability.Loggable;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.GetQuestionnaireById;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.SearchQuestionnaireByFilter;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireView;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireQueryOutPort;
import com.acme.orderquestionnaire.domain.question.Question;
import com.acme.orderquestionnaire.domain.questionnaire.ConfiguredQuestion;
import com.acme.orderquestionnaire.domain.questionnaire.Questionnaire;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.HybridPageRequestUtils;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.engine.pagination.SortDirection;
import com.acme.shared.engine.pagination.SortSpec;
import com.acme.shared.stereotypes.adapter.OutputAdapter;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@OutputAdapter
@Loggable
public class QuestionnaireQueryAdapter implements QuestionnaireQueryOutPort {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final SortSpec DEFAULT_SORT = new SortSpec("id", SortDirection.ASC);
    private static final Map<String, String> SORT_FIELD_MAPPINGS = Map.of(
            "id", "id",
            "channelDistributionId", "channel_distribution_id",
            "journeyDistributionId", "journey_distribution_id",
            "description", "description",
            "status", "status",
            "createdAt", "audit_info.created_at",
            "updatedAt", "audit_info.updated_at"
    );

    private final QuestionnaireQueryRepository questionnaireQueryRepository;
    private final QuestionnaireEntityMapper questionnaireEntityMapper;
    private final QuestionnaireQuestionEntityMapper questionnaireQuestionEntityMapper;
    private final QuestionEntityMapper questionEntityMapper;
    private final MongoTemplate mongoTemplate;

    public QuestionnaireQueryAdapter(QuestionnaireQueryRepository questionnaireQueryRepository,
                                     QuestionnaireEntityMapper questionnaireEntityMapper,
                                     QuestionnaireQuestionEntityMapper questionnaireQuestionEntityMapper,
                                     QuestionEntityMapper questionEntityMapper,
                                     MongoTemplate mongoTemplate) {
        this.questionnaireQueryRepository = Objects.requireNonNull(questionnaireQueryRepository,
                "questionnaireQueryRepository must not be null");
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
    public Optional<QuestionnaireView> findById(GetQuestionnaireById id) {
        if (id == null || id.questionnaireId() == null) {
            return Optional.empty();
        }

        String questionnaireId = id.questionnaireId().id();
        String channelId = id.questionnaireId().channelDistributionId();
        String journeyId = id.questionnaireId().journeyDistributionId();
        if (isBlank(questionnaireId) || isBlank(channelId) || isBlank(journeyId)) {
            return Optional.empty();
        }

        String documentId = questionnaireEntityMapper.toDocumentId(questionnaireId, channelId, journeyId);

        return questionnaireQueryRepository.findById(documentId)
                .map(this::toDomainAggregate)
                .map(QuestionnaireView::from);
    }

    @Override
    public PageResult<QuestionnaireView> findAll(SearchQuestionnaireByFilter criteria) {
        HybridPageRequest pageRequest = HybridPageRequestUtils.resolveOrDefault(
                criteria == null ? null : criteria.pageRequest(),
                DEFAULT_PAGE,
                DEFAULT_SIZE,
                List.of()
        );
        Query baseQuery = buildBaseQuery(criteria);

        if (HybridPageRequestUtils.isCursorMode(pageRequest)) {
            return findAllByCursor(pageRequest, baseQuery);
        }

        return findAllByPage(pageRequest, baseQuery);
    }

    private PageResult<QuestionnaireView> findAllByPage(HybridPageRequest pageRequest, Query baseQuery) {
        int page = pageRequest.page();
        int size = pageRequest.size();
        Sort springSort = MongoQuerySupport.toSpringSort(pageRequest.sort(), SORT_FIELD_MAPPINGS, DEFAULT_SORT);

        Query query = Query.of(baseQuery)
                .with(springSort)
                .skip((long) page * size)
                .limit(size);

        List<QuestionnaireEntity> entities = mongoTemplate.find(query, QuestionnaireEntity.class);
        List<QuestionnaireView> content = toViews(entities);

        long totalElements = mongoTemplate.count(baseQuery, QuestionnaireEntity.class);
        int totalPages = (int) Math.ceil(totalElements / (double) size);
        boolean first = page == 0;
        boolean last = totalPages == 0 || page >= totalPages - 1;

        return PageResult.forPage(content, page, size, totalElements, totalPages, first, last, pageRequest.sort());
    }

    private PageResult<QuestionnaireView> findAllByCursor(HybridPageRequest pageRequest, Query baseQuery) {
        int size = pageRequest.size();

        Query query = Query.of(baseQuery)
                .with(Sort.by(Sort.Order.asc("id")))
                .limit(size + 1);

        if (pageRequest.cursor() != null && !pageRequest.cursor().isBlank()) {
            query.addCriteria(Criteria.where("id").gt(pageRequest.cursor()));
        }

        List<QuestionnaireEntity> fetched = mongoTemplate.find(query, QuestionnaireEntity.class);
        boolean hasNext = fetched.size() > size;
        List<QuestionnaireEntity> contentEntities = hasNext ? fetched.subList(0, size) : fetched;
        String nextCursor = hasNext ? contentEntities.getLast().id() : null;

        List<QuestionnaireView> content = toViews(contentEntities);

        List<SortSpec> appliedSort = List.of(new SortSpec("id", SortDirection.ASC));
        return PageResult.forCursor(content, size, nextCursor, hasNext, appliedSort);
    }

    private Query buildBaseQuery(SearchQuestionnaireByFilter criteria) {
        if (criteria == null) {
            return new Query();
        }

        List<Criteria> all = new ArrayList<>();

        if (criteria.ids() != null && !criteria.ids().isEmpty()) {
            all.add(Criteria.where("id").in(criteria.ids()));
        }
        if (criteria.channelIds() != null && !criteria.channelIds().isEmpty()) {
            all.add(Criteria.where("channel_distribution_id").in(criteria.channelIds()));
        }
        if (criteria.journeyIds() != null && !criteria.journeyIds().isEmpty()) {
            all.add(Criteria.where("journey_distribution_id").in(criteria.journeyIds()));
        }
        MongoQuerySupport.addContainsIfPresent(all, "description", criteria.descriptionContains());
        if (criteria.status() != null) {
            all.add(Criteria.where("status").is(criteria.status()));
        }

        MongoQuerySupport.addAuditInfoFiltersIfPresent(all, criteria.auditInfo(), "audit_info");

        Query query = new Query();
        if (!all.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(all.toArray(new Criteria[0])));
        }
        return query;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private List<QuestionnaireView> toViews(List<QuestionnaireEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        Map<String, List<QuestionnaireQuestionEntity>> linksByQuestionnaire = loadLinksByQuestionnaireDocumentId(entities);
        Map<String, Question> questionsById = loadQuestionsByLinks(linksByQuestionnaire);

        return entities.stream()
                .map(entity -> toDomainAggregate(entity, linksByQuestionnaire, questionsById))
                .map(QuestionnaireView::from)
                .toList();
    }

    private Questionnaire toDomainAggregate(QuestionnaireEntity entity) {
        Map<String, List<QuestionnaireQuestionEntity>> linksByQuestionnaire = loadLinksByQuestionnaireDocumentId(List.of(entity));
        Map<String, Question> questionsById = loadQuestionsByLinks(linksByQuestionnaire);
        return toDomainAggregate(entity, linksByQuestionnaire, questionsById);
    }

    private Questionnaire toDomainAggregate(QuestionnaireEntity entity,
                                            Map<String, List<QuestionnaireQuestionEntity>> linksByQuestionnaire,
                                            Map<String, Question> questionsById) {
        List<QuestionnaireQuestionEntity> links = linksByQuestionnaire.getOrDefault(entity.documentId(), List.of());
        List<ConfiguredQuestion> configuredQuestions = questionnaireQuestionEntityMapper
                .toConfiguredQuestions(links, questionsById);

        return questionnaireEntityMapper.toDomain(entity, configuredQuestions);
    }

    private Map<String, List<QuestionnaireQuestionEntity>> loadLinksByQuestionnaireDocumentId(List<QuestionnaireEntity> entities) {
        List<String> documentIds = entities.stream()
                .map(QuestionnaireEntity::documentId)
                .toList();

        Query linksQuery = Query.query(Criteria.where("questionnaire_document_id").in(documentIds));
        List<QuestionnaireQuestionEntity> links = mongoTemplate.find(linksQuery, QuestionnaireQuestionEntity.class);

        return links.stream().collect(Collectors.groupingBy(
                QuestionnaireQuestionEntity::questionnaireDocumentId,
                LinkedHashMap::new,
                Collectors.toList()
        ));
    }

    private Map<String, Question> loadQuestionsByLinks(
            Map<String, List<QuestionnaireQuestionEntity>> linksByQuestionnaire) {
        Set<String> questionIds = linksByQuestionnaire.values().stream()
                .flatMap(List::stream)
                .map(QuestionnaireQuestionEntity::questionId)
                .collect(Collectors.toSet());

        if (questionIds.isEmpty()) {
            return Map.of();
        }

        Query questionsQuery = Query.query(Criteria.where("_id").in(questionIds));
        List<QuestionEntity> questionEntities = mongoTemplate.find(questionsQuery, QuestionEntity.class);

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

