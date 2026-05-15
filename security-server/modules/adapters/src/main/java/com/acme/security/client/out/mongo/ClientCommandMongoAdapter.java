package com.acme.security.client.out.mongo;

import com.acme.security.client.out.mongo.document.ClientDocument;
import com.acme.security.client.out.mongo.repository.MongoClientRepository;
import com.acme.security.client.port.out.repository.ClientCommandOutPort;
import com.acme.security.client.Client;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageMode;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.engine.pagination.SortDirection;
import com.acme.shared.stereotypes.adapter.OutputAdapter;
import com.acme.shared.vo.TenantId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
@OutputAdapter
public class ClientCommandMongoAdapter implements ClientCommandOutPort {

    private final MongoClientRepository mongoClientRepository;
    private final MongoTemplate mongoTemplate;

    public ClientCommandMongoAdapter(MongoClientRepository mongoClientRepository,
                                     MongoTemplate mongoTemplate) {
        this.mongoClientRepository = Objects.requireNonNull(mongoClientRepository,
                "mongoClientRepository must not be null");
        this.mongoTemplate = Objects.requireNonNull(mongoTemplate,
                "mongoTemplate must not be null");
    }

    @Override
    public Client save(Client client) {
        var clientDocument = ClientDocument.of(client);
        return ClientDocument.map(mongoClientRepository.save(clientDocument));
    }

    @Override
    public Optional<Client> findByClientIdAndTenant(String clientId, TenantId tenantId) {
        return mongoClientRepository
                .findByClientIdAndTenantId(clientId, tenantId.stringValue())
                .map(ClientDocument::map);
    }

    @Override
    public Optional<Client> findById(String id) {
        return mongoClientRepository
                .findById(id)
                .map(ClientDocument::map);
    }

    @Override
    public PageResult<Client> findPageByTenant(TenantId tenantId, HybridPageRequest pageRequest) {
        Query baseQuery = new Query(Criteria.where("tenantId").is(tenantId.stringValue()));
        baseQuery.with(toSort(pageRequest));

        if (pageRequest.mode() == PageMode.CURSOR) {
            int offset = parseCursor(pageRequest.cursor());
            Query cursorQuery = baseQuery.limit(pageRequest.size() + 1).skip(offset);
            var fetched = mongoTemplate.find(cursorQuery, ClientDocument.class);
            boolean hasNext = fetched.size() > pageRequest.size();
            var content = hasNext ? fetched.subList(0, pageRequest.size()) : fetched;
            String nextCursor = hasNext ? String.valueOf(offset + pageRequest.size()) : null;

            return PageResult.forCursor(
                    content.stream().map(ClientDocument::map).toList(),
                    pageRequest.size(),
                    nextCursor,
                    hasNext,
                    pageRequest.sort()
            );
        }

        int page = pageRequest.page();
        int size = pageRequest.size();
        Query pageQuery = baseQuery.limit(size).skip((long) page * size);
        var docs = mongoTemplate.find(pageQuery, ClientDocument.class);
        long totalElements = mongoTemplate.count(new Query(Criteria.where("tenantId").is(tenantId.stringValue())),
                ClientDocument.class);
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        boolean first = page == 0;
        boolean last = totalPages == 0 || page >= totalPages - 1;

        return PageResult.forPage(
                docs.stream().map(ClientDocument::map).toList(),
                page,
                size,
                totalElements,
                totalPages,
                first,
                last,
                pageRequest.sort()
        );
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
