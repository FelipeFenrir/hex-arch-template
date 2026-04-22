package com.acme.orderquestionnaire.adapters.out.mongo.support;

import com.acme.orderquestionnaire.application.audit.dto.queries.SearchByAuditInfo;
import com.acme.shared.engine.pagination.SortDirection;
import com.acme.shared.engine.pagination.SortSpec;
import com.acme.shared.engine.search.TextPatternUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MongoQuerySupport {

    private MongoQuerySupport() {
        throw new IllegalStateException("Utility class");
    }

    public static void addEqualsIfPresent(List<Criteria> target, String field, String value) {
        if (value != null && !value.isBlank()) {
            target.add(Criteria.where(field).is(value));
        }
    }

    public static void addContainsIfPresent(List<Criteria> target, String field, String value) {
        if (value != null && !value.isBlank()) {
            target.add(Criteria.where(field).regex(TextPatternUtils.containsIgnoreCase(value)));
        }
    }

    public static void addDateRangeIfPresent(List<Criteria> target,
                                             String field,
                                             LocalDateTime start,
                                             LocalDateTime end) {
        if (start == null && end == null) {
            return;
        }

        Criteria criteria = Criteria.where(field);
        if (start != null) {
            criteria = criteria.gte(start);
        }
        if (end != null) {
            criteria = criteria.lte(end);
        }

        target.add(criteria);
    }

    public static void addAuditInfoFiltersIfPresent(List<Criteria> target,
                                                    SearchByAuditInfo auditInfo,
                                                    String auditFieldPrefix) {
        if (auditInfo == null) {
            return;
        }

        String prefix = normalizePrefix(auditFieldPrefix);

        addEqualsIfPresent(target, prefix + ".created_by.id", auditInfo.createdById());
        addEqualsIfPresent(target, prefix + ".created_by.reference_code", auditInfo.createdByReferenceCode());
        addContainsIfPresent(target, prefix + ".created_by.name", auditInfo.createdByNameContains());
        addContainsIfPresent(target, prefix + ".created_by.email", auditInfo.createdByEmailContains());
        addDateRangeIfPresent(target, prefix + ".created_at", auditInfo.createdAtStart(), auditInfo.createdAtEnd());

        addEqualsIfPresent(target, prefix + ".updated_by.id", auditInfo.updatedById());
        addEqualsIfPresent(target, prefix + ".updated_by.reference_code", auditInfo.updatedByReferenceCode());
        addContainsIfPresent(target, prefix + ".updated_by.name", auditInfo.updatedByNameContains());
        addContainsIfPresent(target, prefix + ".updated_by.email", auditInfo.updatedByEmailContains());
        addDateRangeIfPresent(target, prefix + ".updated_at", auditInfo.updatedAtStart(), auditInfo.updatedAtEnd());
    }

    public static Sort toSpringSort(List<SortSpec> specs,
                                    Map<String, String> fieldMappings,
                                    SortSpec defaultSort) {
        Objects.requireNonNull(fieldMappings, "fieldMappings must not be null");
        Objects.requireNonNull(defaultSort, "defaultSort must not be null");

        String defaultMongoField = fieldMappings.getOrDefault(defaultSort.field(), defaultSort.field());

        if (specs == null || specs.isEmpty()) {
            return toSpringSort(defaultSort.direction(), defaultMongoField);
        }

        List<Sort.Order> orders = specs.stream()
                .map(spec -> toOrder(spec, fieldMappings, defaultMongoField))
                .toList();

        return Sort.by(orders);
    }

    private static Sort.Order toOrder(SortSpec spec,
                                      Map<String, String> fieldMappings,
                                      String defaultMongoField) {
        String mongoField = fieldMappings.getOrDefault(spec.field(), defaultMongoField);
        return spec.direction() == SortDirection.DESC
                ? Sort.Order.desc(mongoField)
                : Sort.Order.asc(mongoField);
    }

    private static Sort toSpringSort(SortDirection direction, String field) {
        return direction == SortDirection.DESC
                ? Sort.by(Sort.Order.desc(field))
                : Sort.by(Sort.Order.asc(field));
    }

    private static String normalizePrefix(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("auditFieldPrefix must not be null or blank");
        }
        return value.endsWith(".") ? value.substring(0, value.length() - 1) : value;
    }
}

