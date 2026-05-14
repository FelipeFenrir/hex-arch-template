package com.acme.orderquestionnaire.adapters.in.rest.question.entrypoint;

import com.acme.observability.Loggable;
import com.acme.orderquestionnaire.adapters.in.rest.common.ApiCollectionResponse;
import com.acme.orderquestionnaire.adapters.in.rest.common.ApiDataResponse;
import com.acme.orderquestionnaire.adapters.in.rest.common.ApiLinks;
import com.acme.orderquestionnaire.adapters.in.rest.common.ApiMeta;
import com.acme.orderquestionnaire.adapters.in.rest.error.DomainResultException;
import com.acme.orderquestionnaire.adapters.in.rest.question.request.CreateQuestionRequest;
import com.acme.orderquestionnaire.adapters.in.rest.question.request.DeleteQuestionsRequest;
import com.acme.orderquestionnaire.adapters.in.rest.question.request.SearchQuestionRequest;
import com.acme.orderquestionnaire.adapters.in.rest.question.request.UpdateQuestionRequest;
import com.acme.orderquestionnaire.adapters.in.rest.question.response.CreateQuestionResponse;
import com.acme.orderquestionnaire.adapters.in.rest.question.response.DeleteQuestionResponse;
import com.acme.orderquestionnaire.adapters.in.rest.question.response.DeleteQuestionsResponse;
import com.acme.orderquestionnaire.adapters.in.rest.question.response.QuestionResponse;
import com.acme.orderquestionnaire.adapters.in.rest.question.response.UpdateQuestionResponse;
import com.acme.orderquestionnaire.application.common.QueryHandler;
import com.acme.orderquestionnaire.application.question.dto.queries.GetQuestionById;
import com.acme.orderquestionnaire.application.question.dto.queries.SearchQuestionByFilter;
import com.acme.orderquestionnaire.application.question.dto.view.QuestionView;
import com.acme.orderquestionnaire.application.question.error.QuestionErrors;
import com.acme.orderquestionnaire.application.question.port.in.usecase.CreateQuestionUseCase;
import com.acme.orderquestionnaire.application.question.port.in.usecase.DeleteQuestionUseCase;
import com.acme.orderquestionnaire.application.question.port.in.usecase.UpdateQuestionUseCase;
import com.acme.shared.constants.HeaderConstants;
import com.acme.shared.engine.pagination.PageMode;
import com.acme.shared.engine.pagination.PageResult;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@Loggable
public class QuestionController implements QuestionApi {
    private static final String SEARCH_ALLOWED_ORIGIN_PATTERN =
            "${orderquestionnaire.adapters.in.api-rest.cors.question.search.allowed-origin-pattern:http://localhost:*}";
    private static final String CREATE_ALLOWED_ORIGIN_PATTERN =
            "${orderquestionnaire.adapters.in.api-rest.cors.question.create.allowed-origin-pattern:http://localhost:*}";
    private static final String UPDATE_ALLOWED_ORIGIN_PATTERN =
            "${orderquestionnaire.adapters.in.api-rest.cors.question.update.allowed-origin-pattern:http://localhost:*}";
    private static final String DELETE_BY_ID_ALLOWED_ORIGIN_PATTERN =
            "${orderquestionnaire.adapters.in.api-rest.cors.question.delete-by-id.allowed-origin-pattern:http://localhost:*}";
    private static final String DELETE_BATCH_ALLOWED_ORIGIN_PATTERN =
            "${orderquestionnaire.adapters.in.api-rest.cors.question.delete-batch.allowed-origin-pattern:http://localhost:*}";
    private static final String GET_BY_ID_ALLOWED_ORIGIN_PATTERN =
            "${orderquestionnaire.adapters.in.api-rest.cors.question.get-by-id.allowed-origin-pattern:http://localhost:*}";

    private final CreateQuestionUseCase createQuestionUseCase;
    private final UpdateQuestionUseCase updateQuestionUseCase;
    private final DeleteQuestionUseCase deleteQuestionUseCase;
    private final QueryHandler<GetQuestionById, Optional<QuestionView>> getQuestionByIdQueryHandler;
    private final QueryHandler<SearchQuestionByFilter, PageResult<QuestionView>> searchQuestionByFilterQueryHandler;

    public QuestionController(
            CreateQuestionUseCase createQuestionUseCase,
            UpdateQuestionUseCase updateQuestionUseCase,
            DeleteQuestionUseCase deleteQuestionUseCase,
            @Qualifier("getQuestionByIdQueryHandler")
            QueryHandler<GetQuestionById, Optional<QuestionView>> getQuestionByIdQueryHandler,
            @Qualifier("searchQuestionByFilterQueryHandler")
            QueryHandler<SearchQuestionByFilter, PageResult<QuestionView>> searchQuestionByFilterQueryHandler
    ) {
        this.createQuestionUseCase = Objects.requireNonNull(createQuestionUseCase,
                "createQuestionUseCase must not be null");
        this.updateQuestionUseCase = Objects.requireNonNull(updateQuestionUseCase,
                "updateQuestionUseCase must not be null");
        this.deleteQuestionUseCase = Objects.requireNonNull(deleteQuestionUseCase,
                "deleteQuestionUseCase must not be null");
        this.getQuestionByIdQueryHandler = Objects.requireNonNull(getQuestionByIdQueryHandler,
                "getQuestionByIdQueryHandler must not be null");
        this.searchQuestionByFilterQueryHandler = Objects.requireNonNull(searchQuestionByFilterQueryHandler,
                "searchQuestionByFilterQueryHandler must not be null");
    }

    @Override
    @CrossOrigin(
            originPatterns = CREATE_ALLOWED_ORIGIN_PATTERN,
            allowedHeaders = "*",
            exposedHeaders = {HeaderConstants.CORRELATION_HEADER, HeaderConstants.FLOW_HEADER},
            methods = RequestMethod.POST,
            maxAge = 3600
    )
    public ResponseEntity<ApiDataResponse<CreateQuestionResponse>> create(@Valid @RequestBody CreateQuestionRequest request) {
        return createQuestionUseCase.execute(request.toCommand()).fold(
                success -> ResponseEntity.ok(ApiDataResponse.of(CreateQuestionResponse.from(success))),
                this::asDomainFailure
        );
    }

    @Override
    @CrossOrigin(
            originPatterns = UPDATE_ALLOWED_ORIGIN_PATTERN,
            allowedHeaders = "*",
            exposedHeaders = {HeaderConstants.CORRELATION_HEADER, HeaderConstants.FLOW_HEADER},
            methods = RequestMethod.PUT,
            maxAge = 3600
    )
    public ResponseEntity<ApiDataResponse<UpdateQuestionResponse>> update(@PathVariable("id") String id,
                                                                          @Valid @RequestBody UpdateQuestionRequest request) {
        return updateQuestionUseCase.execute(id, request.toCommand()).fold(
                success -> ResponseEntity.ok(ApiDataResponse.of(UpdateQuestionResponse.from(success))),
                this::asDomainFailure
        );
    }

    @Override
    @CrossOrigin(
            originPatterns = DELETE_BY_ID_ALLOWED_ORIGIN_PATTERN,
            allowedHeaders = "*",
            exposedHeaders = {HeaderConstants.CORRELATION_HEADER, HeaderConstants.FLOW_HEADER},
            methods = RequestMethod.DELETE,
            maxAge = 3600
    )
    public ResponseEntity<ApiDataResponse<DeleteQuestionResponse>> deleteById(@PathVariable("id") String id) {
        return deleteQuestionUseCase.execute(id).fold(
                success -> ResponseEntity.ok(ApiDataResponse.of(DeleteQuestionResponse.success(id))),
                this::asDomainFailure
        );
    }

    @Override
    @CrossOrigin(
            originPatterns = DELETE_BATCH_ALLOWED_ORIGIN_PATTERN,
            allowedHeaders = "*",
            exposedHeaders = {HeaderConstants.CORRELATION_HEADER, HeaderConstants.FLOW_HEADER},
            methods = RequestMethod.DELETE,
            maxAge = 3600
    )
    public ResponseEntity<ApiDataResponse<DeleteQuestionsResponse>> deleteBatch(@Valid @RequestBody DeleteQuestionsRequest request) {
        return deleteQuestionUseCase.execute(request.ids()).fold(
                success -> ResponseEntity.ok(ApiDataResponse.of(DeleteQuestionsResponse.from(success, request.ids().size()))),
                this::asDomainFailure
        );
    }

    @Override
    @CrossOrigin(
            originPatterns = GET_BY_ID_ALLOWED_ORIGIN_PATTERN,
            allowedHeaders = "*",
            exposedHeaders = {HeaderConstants.CORRELATION_HEADER, HeaderConstants.FLOW_HEADER},
            methods = RequestMethod.GET,
            maxAge = 3600
    )
    public ResponseEntity<ApiDataResponse<QuestionResponse>> getById(@PathVariable("id") String id) {
        Optional<QuestionView> found = getQuestionByIdQueryHandler.execute(new GetQuestionById(id));
        if (found.isEmpty()) {
            throw new DomainResultException(List.of(QuestionErrors.QUESTION_NOT_FOUND.toDomainError()));
        }
        return ResponseEntity.ok(ApiDataResponse.of(QuestionResponse.from(found.get())));
    }

    @Override
    @CrossOrigin(
            originPatterns = SEARCH_ALLOWED_ORIGIN_PATTERN,
            allowedHeaders = "*",
            exposedHeaders = {HeaderConstants.CORRELATION_HEADER, HeaderConstants.FLOW_HEADER},
            methods = RequestMethod.GET,
            maxAge = 3600
    )
    public ResponseEntity<ApiCollectionResponse<QuestionResponse>> search(@ModelAttribute SearchQuestionRequest request) {
        PageResult<QuestionView> page = searchQuestionByFilterQueryHandler.execute(request.toQuery());

        List<QuestionResponse> data = page.content().stream()
                .map(QuestionResponse::from)
                .toList();

        ApiMeta meta = new ApiMeta(
                page.mode().name(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.hasNext(),
                page.nextCursor()
        );

        ApiLinks links = page.mode() == PageMode.CURSOR
                ? new ApiLinks(nextCursorLink(page), null)
                : new ApiLinks(nextPageLink(page), previousPageLink(page));

        return ResponseEntity.ok(ApiCollectionResponse.of(data, meta, links));
    }

    private <T> ResponseEntity<T> asDomainFailure(List<com.acme.shared.pattern.result.DomainError> errors) {
        throw new DomainResultException(errors);
    }

    private String nextPageLink(PageResult<QuestionView> page) {
        if (!page.hasNext() || page.page() == null) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQueryParam("mode", PageMode.PAGE.name())
                .replaceQueryParam("page", page.page() + 1)
                .replaceQueryParam("cursor")
                .toUriString();
    }

    private String previousPageLink(PageResult<QuestionView> page) {
        if (page.page() == null || page.page() <= 0) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQueryParam("mode", PageMode.PAGE.name())
                .replaceQueryParam("page", page.page() - 1)
                .replaceQueryParam("cursor")
                .toUriString();
    }

    private String nextCursorLink(PageResult<QuestionView> page) {
        if (!page.hasNext() || page.nextCursor() == null || page.nextCursor().isBlank()) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQueryParam("mode", PageMode.CURSOR.name())
                .replaceQueryParam("cursor", page.nextCursor())
                .replaceQueryParam("page")
                .toUriString();
    }
}
