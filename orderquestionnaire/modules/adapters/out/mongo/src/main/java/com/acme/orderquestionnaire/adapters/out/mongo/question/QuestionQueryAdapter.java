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
        List<SortSpec> appliedSort = resolveCursorSort(pageRequest.sort());
        Sort springSort = MongoQuerySupport.toSpringSort(appliedSort, SORT_FIELD_MAPPINGS, DEFAULT_SORT);

        Query query = Query.of(baseQuery)
                .with(springSort)
                .limit(size + 1);

        if (pageRequest.cursor() != null && !pageRequest.cursor().isBlank()) {
            query.addCriteria(buildCursorCriteria(pageRequest.cursor(), appliedSort));
        }

        List<QuestionEntity> fetched = mongoTemplate.find(query, QuestionEntity.class);
        boolean hasNext = fetched.size() > size;
        List<QuestionEntity> contentEntities = hasNext ? fetched.subList(0, size) : fetched;
        String nextCursor = hasNext ? contentEntities.getLast().id() : null;

        List<QuestionView> content = contentEntities.stream()
                .map(questionEntityMapper::toDomain)
                .map(QuestionView::from)
                .toList();

        return PageResult.forCursor(content, size, nextCursor, hasNext, appliedSort);
    }

    private List<SortSpec> resolveCursorSort(List<SortSpec> requestedSort) {
        List<SortSpec> baseSort = requestedSort == null || requestedSort.isEmpty()
                ? List.of(DEFAULT_SORT)
                : requestedSort.stream()
                .map(this::normalizeSortSpec)
                .toList();

        List<SortSpec> effectiveSort = new ArrayList<>();
        for (SortSpec sortSpec : baseSort) {
            boolean alreadyPresent = effectiveSort.stream().anyMatch(existing -> existing.field().equals(sortSpec.field()));
            if (!alreadyPresent) {
                effectiveSort.add(sortSpec);
            }
        }

        boolean hasIdSort = effectiveSort.stream().anyMatch(spec -> spec.field().equals(DEFAULT_SORT.field()));
        if (!hasIdSort) {
            effectiveSort.add(DEFAULT_SORT);
        }

        return List.copyOf(effectiveSort);
    }

    private SortSpec normalizeSortSpec(SortSpec sortSpec) {
        String field = SORT_FIELD_MAPPINGS.containsKey(sortSpec.field()) ? sortSpec.field() : DEFAULT_SORT.field();
        return new SortSpec(field, sortSpec.direction());
    }

    private Criteria buildCursorCriteria(String cursor, List<SortSpec> appliedSort) {
        QuestionEntity anchor = questionQueryRepository.findById(cursor)
                .orElseThrow(() -> new IllegalArgumentException("cursor must reference an existing question"));

        List<Criteria> cursorBranches = new ArrayList<>();
        for (int index = 0; index < appliedSort.size(); index++) {
            List<Criteria> branchCriteria = new ArrayList<>();
            for (int previousIndex = 0; previousIndex < index; previousIndex++) {
                SortSpec previousSort = appliedSort.get(previousIndex);
                branchCriteria.add(Criteria.where(resolveMongoField(previousSort))
                        .is(extractSortValue(anchor, previousSort.field())));
            }

            SortSpec currentSort = appliedSort.get(index);
            branchCriteria.add(buildComparisonCriteria(currentSort, extractSortValue(anchor, currentSort.field())));

            cursorBranches.add(branchCriteria.size() == 1
                    ? branchCriteria.getFirst()
                    : new Criteria().andOperator(branchCriteria.toArray(new Criteria[0])));
        }

        return cursorBranches.size() == 1
                ? cursorBranches.getFirst()
                : new Criteria().orOperator(cursorBranches.toArray(new Criteria[0]));
    }

    private Criteria buildComparisonCriteria(SortSpec sortSpec, Object anchorValue) {
        Criteria criteria = Criteria.where(resolveMongoField(sortSpec));
        return sortSpec.direction() == SortDirection.DESC
                ? criteria.lt(anchorValue)
                : criteria.gt(anchorValue);
    }

    private String resolveMongoField(SortSpec sortSpec) {
        return SORT_FIELD_MAPPINGS.getOrDefault(sortSpec.field(), SORT_FIELD_MAPPINGS.get(DEFAULT_SORT.field()));
    }

    private Object extractSortValue(QuestionEntity anchor, String sortField) {
        return switch (sortField) {
            case "label" -> anchor.label();
            case "status" -> anchor.status();
            case "salesItemReferenceCode" -> anchor.salesItemReferenceCode();
            case "createdAt" -> anchor.auditInfo() == null ? null : anchor.auditInfo().createdAt();
            case "updatedAt" -> anchor.auditInfo() == null ? null : anchor.auditInfo().updatedAt();
            case "id" -> anchor.id();
            default -> anchor.id();
        };
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
