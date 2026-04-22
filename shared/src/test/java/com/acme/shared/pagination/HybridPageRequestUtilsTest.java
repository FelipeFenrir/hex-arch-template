package com.acme.shared.pagination;

import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.HybridPageRequestUtils;
import com.acme.shared.engine.pagination.SortDirection;
import com.acme.shared.engine.pagination.SortSpec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HybridPageRequestUtilsTest {

    @Test
    void shouldIdentifyCursorMode() {
        var request = HybridPageRequest.ofCursor("cursor_1", 10, List.of());

        assertTrue(HybridPageRequestUtils.isCursorMode(request));
    }

    @Test
    void shouldIdentifyPageMode() {
        var request = HybridPageRequest.ofPage(0, 10, List.of());

        assertFalse(HybridPageRequestUtils.isCursorMode(request));
    }

    @Test
    void shouldResolveDefaultRequestWhenNull() {
        var resolved = HybridPageRequestUtils.resolveOrDefault(
                null,
                1,
                20,
                List.of(new SortSpec("id", SortDirection.ASC))
        );

        assertEquals(1, resolved.page());
        assertEquals(20, resolved.size());
        assertEquals("id", resolved.sort().getFirst().field());
    }
}

