package com.acme.shared.engine.pagination;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Pagination contracts")
class PaginationContractsTest {

    @Test
    @DisplayName("HybridPageRequest should create page mode request")
    void shouldCreatePageModeRequest() {
        var request = HybridPageRequest.ofPage(
                0,
                20,
                List.of(new SortSpec("createdAt", SortDirection.DESC))
        );

        assertEquals(0, request.page());
        assertEquals(20, request.size());
        assertEquals(1, request.sort().size());
    }

    @Test
    @DisplayName("HybridPageRequest should reject cursor and page together")
    void shouldRejectCursorAndPageTogether() {
        assertThrows(IllegalArgumentException.class, () ->
                new HybridPageRequest(1, 20, "cursor-1", List.of())
        );
    }

    @Test
    @DisplayName("PageResult should create page mode result")
    void shouldCreatePageResult() {
        var result = PageResult.forPage(
                List.of("a", "b"),
                0,
                2,
                10,
                5,
                true,
                false,
                List.of(new SortSpec("createdAt", SortDirection.DESC))
        );

        assertEquals(PageMode.PAGE, result.mode());
        assertEquals(0, result.page());
        assertEquals(2, result.size());
        assertEquals(10, result.totalElements());
        assertEquals(false, result.last());
        assertEquals(1, result.appliedSort().size());
    }

    @Test
    @DisplayName("PageResult should create cursor mode result")
    void shouldCreateCursorResult() {
        var result = PageResult.forCursor(
                List.of("a"),
                1,
                "cursor-2",
                true,
                List.of(new SortSpec("id", SortDirection.ASC))
        );

        assertEquals(PageMode.CURSOR, result.mode());
        assertEquals(1, result.size());
        assertEquals("cursor-2", result.nextCursor());
        assertEquals(true, result.hasNext());
        assertEquals(1, result.appliedSort().size());
    }
}

