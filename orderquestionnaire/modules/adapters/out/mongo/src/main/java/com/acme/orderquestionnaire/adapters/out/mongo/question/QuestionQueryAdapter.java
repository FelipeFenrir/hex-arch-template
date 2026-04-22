package com.acme.orderquestionnaire.adapters.out.mongo.question;

import com.acme.orderquestionnaire.adapters.out.mongo.question.entity.QuestionEntity;
import com.acme.orderquestionnaire.adapters.out.mongo.question.mapper.QuestionEntityMapper;
import com.acme.orderquestionnaire.adapters.out.mongo.question.repository.QuestionQueryRepository;
import com.acme.orderquestionnaire.adapters.out.mongo.support.MongoQuerySupport;
import com.acme.orderquestionnaire.application.question.dto.queries.GetQuestionById;
import com.acme.orderquestionnaire.application.question.dto.queries.SearchQuestionByFilter;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionView;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionQueryOutPort;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@OutputAdapter
public class QuestionQueryAdapter implements QuestionQueryOutPort {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final SortSpec DEFAULT_SORT = new SortSpec("id", SortDirection.ASC);
    private static final Map<String, String> SORT_FIELD_MAPPINGS = Map.of(
            "id", "id",
            "label", "label",
            "status", "status",
            "salesItemReferenceCode", "sales_item_reference_code",
            "createdAt", "audit_info.created_at",
            "updatedAt", "audit_info.updated_at"
    );

    private final QuestionQueryRepository questionQueryRepository;
    private final QuestionEntityMapper questionEntityMapper;
    private final MongoTemplate mongoTemplate;

    public QuestionQueryAdapter(QuestionQueryRepository questionQueryRepository,
                                QuestionEntityMapper questionEntityMapper,
                                MongoTemplate mongoTemplate) {
        this.questionQueryRepository = Objects.requireNonNull(questionQueryRepository,
                "questionQueryRepository must not be null");
        this.questionEntityMapper = Objects.requireNonNull(questionEntityMapper,
                "questionEntityMapper must not be null");
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate,
                "mongoTemplate must not be null");
    }

    @Override
    public Optional<QuestionView> findById(GetQuestionById id) {
        if (id == null || id.id() == null || id.id().isBlank()) {
            return Optional.empty();
        }

        return questionQueryRepository.findById(id.id())
                .map(questionEntityMapper::toDomain)
                .map(QuestionView::from);
    }

    @Override
    public PageResult<QuestionView> findAll(SearchQuestionByFilter criteria) {
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

    private PageResult<QuestionView> findAllByPage(HybridPageRequest pageRequest, Query baseQuery) {
        int page = pageRequest.page();
        int size = pageRequest.size();
        Sort springSort = MongoQuerySupport.toSpringSort(pageRequest.sort(), SORT_FIELD_MAPPINGS, DEFAULT_SORT);

        Query query = Query.of(baseQuery)
                .with(springSort)
                .skip((long) page * size)
                .limit(size);

        List<QuestionView> content = mongoTemplate.find(query, QuestionEntity.class).stream()
                .map(questionEntityMapper::toDomain)
                .map(QuestionView::from)
                .toList();

        long totalElements = mongoTemplate.count(baseQuery, QuestionEntity.class);
        int totalPages = (int) Math.ceil(totalElements / (double) size);
        boolean first = page == 0;
        boolean last = totalPages == 0 || page >= totalPages - 1;

        return PageResult.forPage(content, page, size, totalElements, totalPages, first, last, pageRequest.sort());
    }

    private PageResult<QuestionView> findAllByCursor(HybridPageRequest pageRequest, Query baseQuery) {
        int size = pageRequest.size();

        Query query = Query.of(baseQuery)
                .with(Sort.by(Sort.Order.asc("id")))
                .limit(size + 1);

        if (pageRequest.cursor() != null && !pageRequest.cursor().isBlank()) {
            query.addCriteria(Criteria.where("id").gt(pageRequest.cursor()));
        }

        List<QuestionEntity> fetched = mongoTemplate.find(query, QuestionEntity.class);
        boolean hasNext = fetched.size() > size;
        List<QuestionEntity> contentEntities = hasNext ? fetched.subList(0, size) : fetched;
        String nextCursor = hasNext ? contentEntities.getLast().id() : null;

        List<QuestionView> content = contentEntities.stream()
                .map(questionEntityMapper::toDomain)
                .map(QuestionView::from)
                .toList();

        List<SortSpec> appliedSort = List.of(new SortSpec("id", SortDirection.ASC));
        return PageResult.forCursor(content, size, nextCursor, hasNext, appliedSort);
    }

    private Query buildBaseQuery(SearchQuestionByFilter criteria) {
        if (criteria == null) {
            return new Query();
        }

        List<Criteria> all = new ArrayList<>();

        if (criteria.ids() != null && !criteria.ids().isEmpty()) {
            all.add(Criteria.where("id").in(criteria.ids()));
        }
        if (criteria.salesItemReferenceCodes() != null && !criteria.salesItemReferenceCodes().isEmpty()) {
            all.add(Criteria.where("sales_item_reference_code").in(criteria.salesItemReferenceCodes()));
        }
        MongoQuerySupport.addContainsIfPresent(all, "label", criteria.labelContains());
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


}
