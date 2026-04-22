package com.acme.orderquestionnaire.adapters.out.mongo.support;

import com.acme.orderquestionnaire.application.audit.dto.queries.SearchByAuditInfo;
import com.acme.shared.engine.pagination.SortDirection;
import com.acme.shared.engine.pagination.SortSpec;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MongoQuerySupportTest {

    private static final Map<String, String> FIELD_MAP = Map.of(
            "id", "id",
            "name", "name",
            "createdAt", "audit_info.created_at"
    );

    @Test
    void shouldAddEqualsCriteriaWhenValueIsPresent() {
        List<Criteria> all = new ArrayList<>();

        MongoQuerySupport.addEqualsIfPresent(all, "field", "value");

        assertEquals(1, all.size());
        Document criteriaObject = all.getFirst().getCriteriaObject();
        assertEquals("value", criteriaObject.get("field"));
    }

    @Test
    void shouldNotAddEqualsCriteriaWhenValueIsBlank() {
        List<Criteria> all = new ArrayList<>();

        MongoQuerySupport.addEqualsIfPresent(all, "field", " ");

        assertTrue(all.isEmpty());
    }

    @Test
    void shouldAddContainsCriteriaWhenValueIsPresent() {
        List<Criteria> all = new ArrayList<>();

        MongoQuerySupport.addContainsIfPresent(all, "label", "Quest");

        assertEquals(1, all.size());
        Object labelValue = all.getFirst().getCriteriaObject().get("label");
        assertTrue(labelValue instanceof Pattern);
    }

    @Test
    void shouldAddDateRangeCriteria() {
        List<Criteria> all = new ArrayList<>();
        LocalDateTime start = LocalDateTime.of(2026, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 30, 23, 59);

        MongoQuerySupport.addDateRangeIfPresent(all, "audit_info.created_at", start, end);

        assertEquals(1, all.size());
        Document range = (Document) all.getFirst().getCriteriaObject().get("audit_info.created_at");
        assertEquals(start, range.get("$gte"));
        assertEquals(end, range.get("$lte"));
    }

    @Test
    void shouldNotAddDateRangeCriteriaWhenBoundsAreNull() {
        List<Criteria> all = new ArrayList<>();

        MongoQuerySupport.addDateRangeIfPresent(all, "audit_info.created_at", null, null);

        assertTrue(all.isEmpty());
    }

    @Test
    void shouldAddAuditFiltersWhenAuditInfoIsPresent() {
        List<Criteria> all = new ArrayList<>();
        SearchByAuditInfo auditInfo = new SearchByAuditInfo(
                "user-created-id",
                "created-ref",
                "created name",
                "created@email.com",
                LocalDateTime.of(2026, 4, 1, 0, 0),
                LocalDateTime.of(2026, 4, 30, 23, 59),
                "user-updated-id",
                "updated-ref",
                "updated name",
                "updated@email.com",
                LocalDateTime.of(2026, 4, 2, 0, 0),
                LocalDateTime.of(2026, 4, 29, 23, 59)
        );

        MongoQuerySupport.addAuditInfoFiltersIfPresent(all, auditInfo, "audit_info");

        assertEquals(10, all.size());
    }

    @Test
    void shouldNotAddAuditFiltersWhenAuditInfoIsNull() {
        List<Criteria> all = new ArrayList<>();

        MongoQuerySupport.addAuditInfoFiltersIfPresent(all, null, "audit_info");

        assertTrue(all.isEmpty());
    }

    @Test
    void shouldThrowWhenAuditPrefixIsBlank() {
        List<Criteria> all = new ArrayList<>();
        SearchByAuditInfo auditInfo = new SearchByAuditInfo(
                "user-created-id", null, null, null,
                null, null,
                null, null, null, null,
                null, null
        );

        assertThrows(IllegalArgumentException.class,
                () -> MongoQuerySupport.addAuditInfoFiltersIfPresent(all, auditInfo, " "));
    }

    @Test
    void shouldUseDefaultSortWhenSpecsAreEmpty() {
        Sort sort = MongoQuerySupport.toSpringSort(List.of(), FIELD_MAP, new SortSpec("id", SortDirection.ASC));

        Sort.Order order = sort.iterator().next();
        assertEquals("id", order.getProperty());
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }

    @Test
    void shouldMapKnownSortField() {
        Sort sort = MongoQuerySupport.toSpringSort(
                List.of(new SortSpec("createdAt", SortDirection.DESC)),
                FIELD_MAP,
                new SortSpec("id", SortDirection.ASC)
        );

        Sort.Order order = sort.iterator().next();
        assertEquals("audit_info.created_at", order.getProperty());
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void shouldFallbackToDefaultSortFieldForUnknownField() {
        Sort sort = MongoQuerySupport.toSpringSort(
                List.of(new SortSpec("unknownField", SortDirection.DESC)),
                FIELD_MAP,
                new SortSpec("id", SortDirection.ASC)
        );

        Sort.Order order = sort.iterator().next();
        assertEquals("id", order.getProperty());
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }
}

