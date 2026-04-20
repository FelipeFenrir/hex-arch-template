package com.acme.orderquestionnaire.application.question.service;

import com.acme.orderquestionnaire.application.question.dto.queries.GetQuestionById;
import com.acme.orderquestionnaire.application.question.dto.queries.SearchQuestionByFilter;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionView;
import com.acme.orderquestionnaire.application.question.port.out.repository.QuestionQueryOutPort;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@UnitTest
@DisplayName("Question handlers")
class QuestionHandlerTest {

    private QuestionQueryOutPort queryPort;

    @BeforeEach
    void setUp() {
        queryPort = mock(QuestionQueryOutPort.class);
    }

    // ── GetQuestionByIdHandler ────────────────────────────────────────────────

    @Test
    @DisplayName("GetQuestionByIdHandler: should throw when repository is null")
    void getByIdShouldThrowWhenRepositoryNull() {
        assertThrows(NullPointerException.class, () -> new GetQuestionByIdHandler(null));
    }

    @Test
    @DisplayName("GetQuestionByIdHandler: should return empty when id is blank")
    void getByIdShouldReturnEmptyForBlankId() {
        var handler = new GetQuestionByIdHandler(queryPort);
        assertTrue(handler.execute(new GetQuestionById("")).isEmpty());
        verify(queryPort, never()).findById(any());
    }

    @Test
    @DisplayName("GetQuestionByIdHandler: should return empty when id is null")
    void getByIdShouldReturnEmptyForNullId() {
        var handler = new GetQuestionByIdHandler(queryPort);
        assertTrue(handler.execute(new GetQuestionById(null)).isEmpty());
        verify(queryPort, never()).findById(any());
    }

    @Test
    @DisplayName("GetQuestionByIdHandler: should delegate to port when id is valid")
    void getByIdShouldDelegateToPort() {
        var query = new GetQuestionById("q_age");
        var view = mock(QuestionView.class);
        when(queryPort.findById(query)).thenReturn(Optional.of(view));
        var handler = new GetQuestionByIdHandler(queryPort);
        assertEquals(Optional.of(view), handler.execute(query));
    }

    // ── SearchQuestionByFilterHandler ─────────────────────────────────────────

    @Test
    @DisplayName("SearchQuestionByFilterHandler: should throw when repository is null")
    void searchShouldThrowWhenRepositoryNull() {
        assertThrows(NullPointerException.class, () -> new SearchQuestionByFilterHandler(null));
    }

    @Test
    @DisplayName("SearchQuestionByFilterHandler: should delegate to port")
    void searchShouldDelegateToPort() {
        var filter = new SearchQuestionByFilter(
                null,
                null,
                null,
                null,
                null,
                HybridPageRequest.ofPage(0, 10, null)
        );
        var result = PageResult.<QuestionView>forPage(
                java.util.List.of(),
                0,
                10,
                0,
                0,
                true,
                true,
                java.util.List.of()
        );
        when(queryPort.findAll(filter)).thenReturn(result);
        var handler = new SearchQuestionByFilterHandler(queryPort);
        assertEquals(result, handler.execute(filter));
    }
}

