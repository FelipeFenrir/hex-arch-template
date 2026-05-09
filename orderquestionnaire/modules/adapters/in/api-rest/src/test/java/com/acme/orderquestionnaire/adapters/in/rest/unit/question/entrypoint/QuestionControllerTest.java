package com.acme.orderquestionnaire.adapters.in.rest.unit.question.entrypoint;

import com.acme.orderquestionnaire.adapters.in.rest.common.ApiCollectionResponse;
import com.acme.orderquestionnaire.adapters.in.rest.error.DomainResultException;
import com.acme.orderquestionnaire.adapters.in.rest.question.entrypoint.QuestionController;
import com.acme.orderquestionnaire.adapters.in.rest.question.request.CreateQuestionRequest;
import com.acme.orderquestionnaire.adapters.in.rest.question.request.SearchQuestionRequest;
import com.acme.orderquestionnaire.adapters.in.rest.audit.request.AuditUserRequest;
import com.acme.orderquestionnaire.application.audit.dto.view.UserView;
import com.acme.orderquestionnaire.application.common.QueryHandler;
import com.acme.orderquestionnaire.application.question.dto.queries.GetQuestionById;
import com.acme.orderquestionnaire.application.question.dto.queries.SearchQuestionByFilter;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionCreatedView;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionView;
import com.acme.orderquestionnaire.application.question.port.in.usecase.CreateQuestionUseCase;
import com.acme.orderquestionnaire.application.question.port.in.usecase.DeleteQuestionUseCase;
import com.acme.orderquestionnaire.application.question.port.in.usecase.UpdateQuestionUseCase;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.engine.pagination.PageMode;
import com.acme.shared.pattern.result.DomainError;
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
@DisplayName("QuestionController")
class QuestionControllerTest {

    @Mock
    private CreateQuestionUseCase createQuestionUseCase;
    @Mock
    private UpdateQuestionUseCase updateQuestionUseCase;
    @Mock
    private DeleteQuestionUseCase deleteQuestionUseCase;
    @Mock
    private QueryHandler<GetQuestionById, Optional<QuestionView>> getByIdHandler;
    @Mock
    private QueryHandler<SearchQuestionByFilter, PageResult<QuestionView>> searchHandler;

    @Test
    @DisplayName("should wrap create success into data envelope")
    void shouldWrapCreateSuccessIntoDataEnvelope() {
        QuestionController controller = new QuestionController(
                createQuestionUseCase,
                updateQuestionUseCase,
                deleteQuestionUseCase,
                getByIdHandler,
                searchHandler
        );

        QuestionCreatedView created = new QuestionCreatedView(
                "q1",
                "Label",
                "DRAFT",
                "sales",
                new UserView("u1", "r1", "User", "u@acme.test"),
                LocalDateTime.now(),
                null,
                null
        );

        when(createQuestionUseCase.execute(any())).thenReturn(Result.success(created));

        var response = controller.create(new CreateQuestionRequest(
                "q1",
                "Label",
                "sales",
                new AuditUserRequest("019dff07-5f02-70d4-8680-f8dc34fd5fb9", "ref", "User", "user@acme.test")
        ));

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("q1", response.getBody().data().id());
    }

    @Test
    @DisplayName("should throw domain exception on create failure")
    void shouldThrowDomainExceptionOnCreateFailure() {
        QuestionController controller = new QuestionController(
                createQuestionUseCase,
                updateQuestionUseCase,
                deleteQuestionUseCase,
                getByIdHandler,
                searchHandler
        );

        when(createQuestionUseCase.execute(any()))
                .thenReturn(Result.failure(List.of(new DomainError("INVALID_COMMAND", "invalid command"))));

        assertThrows(DomainResultException.class, () -> controller.create(new CreateQuestionRequest(
                "q1",
                "Label",
                "sales",
                new AuditUserRequest("019dff07-5f02-70d4-8680-f8dc34fd5fb9", "ref", "User", "user@acme.test")
        )));
    }

    @Test
    @DisplayName("should return page meta and links for search")
    void shouldReturnPageMetaAndLinksForSearch() {
        QuestionController controller = new QuestionController(
                createQuestionUseCase,
                updateQuestionUseCase,
                deleteQuestionUseCase,
                getByIdHandler,
                searchHandler
        );

        QuestionView view = new QuestionView("q1", "Label", "DRAFT", "sales", null, null, null, null);
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

        MockHttpServletRequest servletRequest = new MockHttpServletRequest("GET", "/api/v1/questions");
        servletRequest.setQueryString("page=0&size=1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));

        ApiCollectionResponse<?> response = controller.search(new SearchQuestionRequest(
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
}

