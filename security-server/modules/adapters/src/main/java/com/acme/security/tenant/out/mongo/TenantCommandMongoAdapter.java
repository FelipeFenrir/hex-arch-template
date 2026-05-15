package com.acme.security.tenant.out.mongo;

import com.acme.security.tenant.out.mongo.document.TenantDocument;
import com.acme.security.tenant.out.mongo.repository.MongoTenantRepository;
import com.acme.security.tenant.port.out.repository.TenantCommandOutPort;
import com.acme.security.tenant.Tenant;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageMode;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.engine.pagination.SortDirection;
import com.acme.shared.stereotypes.adapter.OutputAdapter;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
@OutputAdapter
public class TenantCommandMongoAdapter implements TenantCommandOutPort {
    private final MongoTenantRepository mongoTenantRepository;
    private final MongoTemplate mongoTemplate;

    public TenantCommandMongoAdapter(MongoTenantRepository mongoTenantRepository,
                                     MongoTemplate mongoTemplate) {
        this.mongoTenantRepository = Objects.requireNonNull(mongoTenantRepository,
                "mongoTenantRepository must not be null");
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate,
                "mongoTemplate must not be null");
    }

    @Override
    public Tenant save(Tenant tenant) {
        TenantDocument saved = mongoTenantRepository.save(TenantDocument.of(tenant));
        return TenantDocument.map(saved);
    }

    @Override
    public Optional<Tenant> findById(String id) {
        return mongoTenantRepository.findById(id)
                .map(TenantDocument::map);
    }

    @Override
    public Optional<Tenant> findBySlug(String slug) {
        return mongoTenantRepository.findBySlug(slug)
                .map(TenantDocument::map);
    }

    @Override
    public PageResult<Tenant> findPage(HybridPageRequest pageRequest) {
        Query baseQuery = new Query().with(toSort(pageRequest));

        if (pageRequest.mode() == PageMode.CURSOR) {
            int offset = parseCursor(pageRequest.cursor());
            Query cursorQuery = baseQuery.limit(pageRequest.size() + 1).skip(offset);
            var fetched = mongoTemplate.find(cursorQuery, TenantDocument.class);
            boolean hasNext = fetched.size() > pageRequest.size();
            var content = hasNext ? fetched.subList(0, pageRequest.size()) : fetched;
            String nextCursor = hasNext ? String.valueOf(offset + pageRequest.size()) : null;

            return PageResult.forCursor(
                    content.stream().map(TenantDocument::map).toList(),
                    pageRequest.size(),
                    nextCursor,
                    hasNext,
                    pageRequest.sort()
            );
        }

        int page = pageRequest.page();
        int size = pageRequest.size();
        Query pageQuery = baseQuery.limit(size).skip((long) page * size);
        var docs = mongoTemplate.find(pageQuery, TenantDocument.class);
        long totalElements = mongoTemplate.count(new Query(), TenantDocument.class);
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        boolean first = page == 0;
        boolean last = totalPages == 0 || page >= totalPages - 1;

        return PageResult.forPage(
                docs.stream().map(TenantDocument::map).toList(),
                page,
                size,
                totalElements,
                totalPages,
                first,
                last,
                pageRequest.sort()
        );
    }

    @Override
    public boolean existsBySlug(String slug) {
        return mongoTenantRepository.existsBySlug(slug);
    }

    @Override
    public void deleteById(String id) {
        mongoTenantRepository.deleteById(id);
    }

    private Sort toSort(HybridPageRequest pageRequest) {
        if (pageRequest.sort() == null || pageRequest.sort().isEmpty()) {
            return Sort.by(Sort.Direction.ASC, "_id");
        }

        Sort sort = Sort.unsorted();
        for (var spec : pageRequest.sort()) {
            Sort.Direction direction = spec.direction() == SortDirection.DESC ? Sort.Direction.DESC : Sort.Direction.ASC;
            String field = switch (spec.field()) {
                case "id" -> "_id";
                default -> spec.field();
            };
            sort = sort.and(Sort.by(direction, field));
        }
        return sort;
    }

    private int parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(cursor));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid cursor value", exception);
        }
    }
}
