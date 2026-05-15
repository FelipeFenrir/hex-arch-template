package com.acme.security.user.out.mongo;

import com.acme.security.user.out.mongo.document.UserDocument;
import com.acme.security.user.out.mongo.repository.MongoUserRepository;
import com.acme.security.user.port.out.repository.UserCommandOutPort;
import com.acme.security.user.User;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageMode;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.engine.pagination.SortDirection;
import com.acme.shared.stereotypes.adapter.OutputAdapter;
import com.acme.shared.vo.TenantId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
@OutputAdapter
public class UserCommandMongoAdapter implements UserCommandOutPort {

    private final MongoUserRepository mongoUserRepository;
    private final MongoTemplate mongoTemplate;

    public UserCommandMongoAdapter(MongoUserRepository mongoUserRepository,
                                   MongoTemplate mongoTemplate) {
        this.mongoUserRepository = Objects.requireNonNull(mongoUserRepository,
                "mongoUserRepository must not be null");
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate,
                "mongoTemplate must not be null");
    }

    @Override
    public User save(User user) {
        var userDocument = UserDocument.of(user);
        return UserDocument.map(mongoUserRepository.save(userDocument));
    }

    @Override
    public Optional<User> findByUsernameAndTenant(String username, TenantId tenantId) {
        return mongoUserRepository
                .findByUsernameAndTenantId(username, tenantId.stringValue())
                .map(UserDocument::map);
    }

    @Override
    public Optional<User> findByIdAndTenant(String id, TenantId tenantId) {
        return mongoUserRepository
                .findByIdAndTenantId(id, tenantId.stringValue())
                .map(UserDocument::map);
    }

    @Override
    public PageResult<User> findPageByTenant(TenantId tenantId, HybridPageRequest pageRequest) {
        Query baseQuery = new Query(Criteria.where("tenantId").is(tenantId.stringValue()));
        baseQuery.with(toSort(pageRequest));

        if (pageRequest.mode() == PageMode.CURSOR) {
            int offset = parseCursor(pageRequest.cursor());
            Query cursorQuery = baseQuery.limit(pageRequest.size() + 1).skip(offset);
            var fetched = mongoTemplate.find(cursorQuery, UserDocument.class);
            boolean hasNext = fetched.size() > pageRequest.size();
            var content = hasNext ? fetched.subList(0, pageRequest.size()) : fetched;
            String nextCursor = hasNext ? String.valueOf(offset + pageRequest.size()) : null;

            return PageResult.forCursor(
                    content.stream().map(UserDocument::map).toList(),
                    pageRequest.size(),
                    nextCursor,
                    hasNext,
                    pageRequest.sort()
            );
        }

        int page = pageRequest.page();
        int size = pageRequest.size();
        Query pageQuery = baseQuery.limit(size).skip((long) page * size);
        var docs = mongoTemplate.find(pageQuery, UserDocument.class);
        long totalElements = mongoTemplate.count(new Query(Criteria.where("tenantId").is(tenantId.stringValue())),
                UserDocument.class);
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        boolean first = page == 0;
        boolean last = totalPages == 0 || page >= totalPages - 1;

        return PageResult.forPage(
                docs.stream().map(UserDocument::map).toList(),
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
    public boolean existsByIdAndTenant(String id, TenantId tenantId) {
        return mongoUserRepository.existsByIdAndTenantId(id, tenantId.stringValue());
    }

    @Override
    public void deleteByIdAndTenant(String id, TenantId tenantId) {
        mongoUserRepository.deleteByIdAndTenantId(id, tenantId.stringValue());
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
