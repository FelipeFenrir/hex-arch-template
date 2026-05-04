package com.acme.orderquestionnaire.application.unit.questionnaire.service;

import com.acme.orderquestionnaire.application.questionnaire.dto.queries.GetQuestionnaireById;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.SearchQuestionnaireByFilter;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireView;
import com.acme.orderquestionnaire.application.questionnaire.port.out.repository.QuestionnaireQueryOutPort;
import com.acme.orderquestionnaire.application.questionnaire.service.GetQuestionnaireByIdHandler;
import com.acme.orderquestionnaire.application.questionnaire.service.SearchQuestionnaireByFilterHandler;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.engine.pagination.HybridPageRequest;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@DisplayName("Questionnaire handlers")
class QuestionnaireHandlerTest {

    private QuestionnaireQueryOutPort queryPort;

    @BeforeEach
    void setUp() {
        queryPort = mock(QuestionnaireQueryOutPort.class);
    }

    // ── GetQuestionnaireByIdHandler ───────────────────────────────────────────

    @Test
    @DisplayName("GetQuestionnaireByIdHandler: should throw when repository is null")
    void getByIdShouldThrowWhenRepositoryNull() {
        assertThrows(NullPointerException.class, () -> new GetQuestionnaireByIdHandler(null));
    }

    @Test
    @DisplayName("GetQuestionnaireByIdHandler: should delegate to port")
    void getByIdShouldDelegateToPort() {
        var query = new GetQuestionnaireById(QuestionnaireId.of("q_001", "APP", "JOURNEY"));
        var view = mock(QuestionnaireView.class);
        when(queryPort.findById(query)).thenReturn(Optional.of(view));
        var handler = new GetQuestionnaireByIdHandler(queryPort);
        assertEquals(Optional.of(view), handler.execute(query));
    }

    // ── SearchQuestionnaireByFilterHandler ───────────────────────────────────

    @Test
    @DisplayName("SearchQuestionnaireByFilterHandler: should throw when repository is null")
    void searchShouldThrowWhenRepositoryNull() {
        assertThrows(NullPointerException.class, () -> new SearchQuestionnaireByFilterHandler(null));
    }

    @Test
    @DisplayName("SearchQuestionnaireByFilterHandler: should delegate to port")
    void searchShouldDelegateToPort() {
        var filter = new SearchQuestionnaireByFilter(
                null,
                null,
                null,
                null,
                null,
                null,
                HybridPageRequest.ofPage(0, 10, null)
        );
        var result = PageResult.<QuestionnaireView>forPage(
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
        var handler = new SearchQuestionnaireByFilterHandler(queryPort);
        assertEquals(result, handler.execute(filter));
    }
}

