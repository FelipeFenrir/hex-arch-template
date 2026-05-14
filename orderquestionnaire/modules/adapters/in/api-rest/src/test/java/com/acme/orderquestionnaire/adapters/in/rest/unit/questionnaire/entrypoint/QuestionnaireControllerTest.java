package com.acme.orderquestionnaire.adapters.in.rest.unit.questionnaire.entrypoint;

import com.acme.orderquestionnaire.adapters.in.rest.common.ApiCollectionResponse;
import com.acme.orderquestionnaire.adapters.in.rest.audit.request.AuditUserRequest;
import com.acme.orderquestionnaire.adapters.in.rest.error.DomainResultException;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.entrypoint.QuestionnaireController;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request.CreateQuestionnaireRequest;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request.QuestionnaireCompositeKeyRequest;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request.SearchQuestionnaireRequest;
import com.acme.orderquestionnaire.application.audit.dto.view.UserView;
import com.acme.orderquestionnaire.application.common.QueryHandler;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.GetQuestionnaireById;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.SearchQuestionnaireByFilter;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireCreatedView;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireView;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.CreateQuestionnaireUseCase;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.DeleteQuestionnaireUseCase;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.UpdateQuestionnaireUseCase;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.engine.pagination.PageMode;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.pattern.result.Result;
import com.acme.shared.stereotypes.test.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@UnitTest
@ExtendWith(MockitoExtension.class)
@DisplayName("QuestionnaireController")
class QuestionnaireControllerTest {

    @Mock
    private CreateQuestionnaireUseCase createQuestionnaireUseCase;
    @Mock
    private UpdateQuestionnaireUseCase updateQuestionnaireUseCase;
    @Mock
    private DeleteQuestionnaireUseCase deleteQuestionnaireUseCase;
    @Mock
    private QueryHandler<GetQuestionnaireById, Optional<QuestionnaireView>> getByIdHandler;
    @Mock
    private QueryHandler<SearchQuestionnaireByFilter, PageResult<QuestionnaireView>> searchHandler;

    @Test
    @DisplayName("should wrap create success into data envelope")
    void shouldWrapCreateSuccessIntoDataEnvelope() {
        QuestionnaireController controller = new QuestionnaireController(
                createQuestionnaireUseCase,
                updateQuestionnaireUseCase,
                deleteQuestionnaireUseCase,
                getByIdHandler,
                searchHandler
        );

        QuestionnaireCreatedView created = new QuestionnaireCreatedView(
                "qn_one",
                "ch1",
                "jr1",
                "Desc",
                "DRAFT",
                new UserView("u1", "r1", "User", "u@acme.test"),
                LocalDateTime.now(),
                null,
                null
        );

        when(createQuestionnaireUseCase.execute(any())).thenReturn(Result.success(created));

        var response = controller.create(new CreateQuestionnaireRequest(
                "qn_one",
                "ch1",
                "jr1",
                "Desc",
                new AuditUserRequest("019dff07-5f02-70d4-8680-f8dc34fd5fb9", "ref", "User", "user@acme.test")
        ));

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("qn_one", response.getBody().data().id());
        assertNotNull(response.getBody().data().configuredQuestions());
    }

    @Test
    @DisplayName("should return page meta and links for search")
    void shouldReturnPageMetaAndLinksForSearch() {
        QuestionnaireController controller = new QuestionnaireController(
                createQuestionnaireUseCase,
                updateQuestionnaireUseCase,
                deleteQuestionnaireUseCase,
                getByIdHandler,
                searchHandler
        );

        QuestionnaireView view = new QuestionnaireView(
                QuestionnaireId.of("qn_one", "ch1", "jr1"),
                "Desc",
                "DRAFT",
                List.of(),
                null,
                null,
                null,
                null
        );

        when(searchHandler.execute(any())).thenReturn(PageResult.forPage(
                List.of(view),
                0,
                1,
                3,
                3,
                true,
                false,
                List.of()
        ));

        MockHttpServletRequest servletRequest = new MockHttpServletRequest("GET", "/api/v1/questionnaires");
        servletRequest.setQueryString("page=0&size=1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));

        ApiCollectionResponse<?> response = controller.search(new SearchQuestionnaireRequest(
                null,
                null,
                null,
                null,
                null,
                PageMode.PAGE,
                0,
                1,
                null,
                null
        )).getBody();

        assertNotNull(response);
        assertEquals("PAGE", response.meta().mode());
        assertNotNull(response.links().next());
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("should throw domain exception when questionnaire is missing")
    void shouldThrowDomainExceptionWhenQuestionnaireIsMissing() {
        QuestionnaireController controller = new QuestionnaireController(
                createQuestionnaireUseCase,
                updateQuestionnaireUseCase,
                deleteQuestionnaireUseCase,
                getByIdHandler,
                searchHandler
        );

        when(getByIdHandler.execute(any())).thenReturn(Optional.empty());

        assertThrows(DomainResultException.class,
                () -> controller.getById("qn_missing", new QuestionnaireCompositeKeyRequest("ch1", "jr1")));
    }
}

