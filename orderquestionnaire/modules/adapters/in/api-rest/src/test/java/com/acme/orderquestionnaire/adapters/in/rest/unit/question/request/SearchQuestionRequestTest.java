package com.acme.orderquestionnaire.adapters.in.rest.unit.question.request;

import com.acme.orderquestionnaire.adapters.in.rest.question.request.SearchQuestionRequest;
import com.acme.shared.engine.pagination.PageMode;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
@DisplayName("SearchQuestionRequest")
class SearchQuestionRequestTest {

    @Test
    @DisplayName("should create page request when cursor is absent")
    void shouldCreatePageRequestWhenCursorIsAbsent() {
        SearchQuestionRequest request = new SearchQuestionRequest(
                null,
                null,
                null,
                null,
                PageMode.PAGE,
                2,
                20,
                null,
                List.of("label,desc")
        );

        var query = request.toQuery();

        assertEquals(2, query.pageRequest().page());
        assertEquals(20, query.pageRequest().size());
        assertEquals("label", query.pageRequest().sort().getFirst().field());
    }

    @Test
    @DisplayName("should create cursor request when cursor is present")
    void shouldCreateCursorRequestWhenCursorIsPresent() {
        SearchQuestionRequest request = new SearchQuestionRequest(
                null,
                null,
                null,
                null,
                PageMode.CURSOR,
                null,
                5,
                "q42",
                List.of("id,asc")
        );

        var query = request.toQuery();

        assertEquals(null, query.pageRequest().page());
        assertEquals("q42", query.pageRequest().cursor());
        assertEquals(PageMode.CURSOR, query.pageRequest().mode());
    }

    @Test
    @DisplayName("should allow first cursor page without cursor token")
    void shouldAllowFirstCursorPageWithoutCursorToken() {
        SearchQuestionRequest request = new SearchQuestionRequest(
                null,
                null,
                null,
                null,
                PageMode.CURSOR,
                null,
                5,
                null,
                List.of("id,asc")
        );

        var query = request.toQuery();

        assertEquals(PageMode.CURSOR, query.pageRequest().mode());
        assertEquals(null, query.pageRequest().cursor());
        assertEquals(null, query.pageRequest().page());
    }

    @Test
    @DisplayName("should reject cursor mode with page parameter")
    void shouldRejectCursorModeWithPageParameter() {
        SearchQuestionRequest request = new SearchQuestionRequest(
                null,
                null,
                null,
                null,
                PageMode.CURSOR,
                1,
                5,
                null,
                List.of("id,asc")
        );

        assertThrows(IllegalArgumentException.class, request::toQuery);
    }

    @Test
    @DisplayName("should reject page mode with cursor token")
    void shouldRejectPageModeWithCursorToken() {
        SearchQuestionRequest request = new SearchQuestionRequest(
                null,
                null,
                null,
                null,
                PageMode.PAGE,
                0,
                5,
                "q42",
                List.of("id,asc")
        );

        assertThrows(IllegalArgumentException.class, request::toQuery);
    }
}

