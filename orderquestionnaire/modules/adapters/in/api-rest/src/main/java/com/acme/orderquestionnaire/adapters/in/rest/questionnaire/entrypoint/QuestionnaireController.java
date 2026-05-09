package com.acme.orderquestionnaire.adapters.in.rest.questionnaire.entrypoint;

import com.acme.observability.Loggable;
import com.acme.orderquestionnaire.adapters.in.rest.common.ApiCollectionResponse;
import com.acme.orderquestionnaire.adapters.in.rest.common.ApiDataResponse;
import com.acme.orderquestionnaire.adapters.in.rest.common.ApiLinks;
import com.acme.orderquestionnaire.adapters.in.rest.common.ApiMeta;
import com.acme.orderquestionnaire.adapters.in.rest.error.DomainResultException;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request.CreateQuestionnaireRequest;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request.DeleteQuestionnairesRequest;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request.QuestionnaireCompositeKeyRequest;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request.SearchQuestionnaireRequest;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request.UpdateQuestionnaireRequest;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.response.DeleteQuestionnaireResponse;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.response.DeleteQuestionnairesResponse;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.response.QuestionnaireResponse;
import com.acme.orderquestionnaire.application.common.QueryHandler;
import com.acme.orderquestionnaire.application.questionnaire.dto.command.DeleteQuestionnaireCommand;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.GetQuestionnaireById;
import com.acme.orderquestionnaire.application.questionnaire.dto.queries.SearchQuestionnaireByFilter;
import com.acme.orderquestionnaire.application.questionnaire.dto.view.QuestionnaireView;
import com.acme.orderquestionnaire.application.questionnaire.error.QuestionnaireErrors;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.CreateQuestionnaireUseCase;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.DeleteQuestionnaireUseCase;
import com.acme.orderquestionnaire.application.questionnaire.port.in.usecase.UpdateQuestionnaireUseCase;
import com.acme.orderquestionnaire.domain.questionnaire.vo.QuestionnaireId;
import com.acme.shared.constants.HeaderConstants;
import com.acme.shared.engine.pagination.PageMode;
import com.acme.shared.engine.pagination.PageResult;
import com.acme.shared.pattern.result.DomainError;
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
public class QuestionnaireController implements QuestionnaireApi {
    private static final String SEARCH_ALLOWED_ORIGIN_PATTERN =
            "${orderquestionnaire.adapters.in.api-rest.cors.questionnaire.search.allowed-origin-pattern:http://localhost:*}";
    private static final String CREATE_ALLOWED_ORIGIN_PATTERN =
            "${orderquestionnaire.adapters.in.api-rest.cors.questionnaire.create.allowed-origin-pattern:http://localhost:*}";
    private static final String UPDATE_ALLOWED_ORIGIN_PATTERN =
            "${orderquestionnaire.adapters.in.api-rest.cors.questionnaire.update.allowed-origin-pattern:http://localhost:*}";
    private static final String DELETE_BY_ID_ALLOWED_ORIGIN_PATTERN =
            "${orderquestionnaire.adapters.in.api-rest.cors.questionnaire.delete-by-id.allowed-origin-pattern:http://localhost:*}";
    private static final String DELETE_BATCH_ALLOWED_ORIGIN_PATTERN =
            "${orderquestionnaire.adapters.in.api-rest.cors.questionnaire.delete-batch.allowed-origin-pattern:http://localhost:*}";
    private static final String GET_BY_ID_ALLOWED_ORIGIN_PATTERN =
            "${orderquestionnaire.adapters.in.api-rest.cors.questionnaire.get-by-id.allowed-origin-pattern:http://localhost:*}";

    private final CreateQuestionnaireUseCase createQuestionnaireUseCase;
    private final UpdateQuestionnaireUseCase updateQuestionnaireUseCase;
    private final DeleteQuestionnaireUseCase deleteQuestionnaireUseCase;
    private final QueryHandler<GetQuestionnaireById, Optional<QuestionnaireView>> getQuestionnaireByIdQueryHandler;
    private final QueryHandler<SearchQuestionnaireByFilter, PageResult<QuestionnaireView>> searchQuestionnaireByFilterQueryHandler;

    public QuestionnaireController(
            CreateQuestionnaireUseCase createQuestionnaireUseCase,
            UpdateQuestionnaireUseCase updateQuestionnaireUseCase,
            DeleteQuestionnaireUseCase deleteQuestionnaireUseCase,
            @Qualifier("getQuestionnaireByIdQueryHandler")
            QueryHandler<GetQuestionnaireById, Optional<QuestionnaireView>> getQuestionnaireByIdQueryHandler,
            @Qualifier("searchQuestionnaireByFilterQueryHandler")
            QueryHandler<SearchQuestionnaireByFilter, PageResult<QuestionnaireView>> searchQuestionnaireByFilterQueryHandler
    ) {
        this.createQuestionnaireUseCase = Objects.requireNonNull(createQuestionnaireUseCase,
                "createQuestionnaireUseCase must not be null");
        this.updateQuestionnaireUseCase = Objects.requireNonNull(updateQuestionnaireUseCase,
                "updateQuestionnaireUseCase must not be null");
        this.deleteQuestionnaireUseCase = Objects.requireNonNull(deleteQuestionnaireUseCase,
                "deleteQuestionnaireUseCase must not be null");
        this.getQuestionnaireByIdQueryHandler = Objects.requireNonNull(getQuestionnaireByIdQueryHandler,
                "getQuestionnaireByIdQueryHandler must not be null");
        this.searchQuestionnaireByFilterQueryHandler = Objects.requireNonNull(searchQuestionnaireByFilterQueryHandler,
                "searchQuestionnaireByFilterQueryHandler must not be null");
    }

    @Override
    @CrossOrigin(
            originPatterns = CREATE_ALLOWED_ORIGIN_PATTERN,
            allowedHeaders = "*",
            exposedHeaders = {HeaderConstants.CORRELATION_HEADER, HeaderConstants.FLOW_HEADER},
            methods = RequestMethod.POST,
            maxAge = 3600
    )
    public ResponseEntity<ApiDataResponse<QuestionnaireResponse>> create(@Valid @RequestBody CreateQuestionnaireRequest request) {
        return createQuestionnaireUseCase.execute(request.toCommand()).fold(
                success -> ResponseEntity.ok(ApiDataResponse.of(QuestionnaireResponse.from(success))),
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
    public ResponseEntity<ApiDataResponse<QuestionnaireResponse>> update(
            @PathVariable("id") String id,
            @Valid @ModelAttribute QuestionnaireCompositeKeyRequest key,
            @Valid @RequestBody UpdateQuestionnaireRequest request
    ) {
        return updateQuestionnaireUseCase.execute(request.toCommand(id, key.channelId(), key.journeyId())).fold(
                success -> ResponseEntity.ok(ApiDataResponse.of(QuestionnaireResponse.from(success))),
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
    public ResponseEntity<ApiDataResponse<DeleteQuestionnaireResponse>> deleteById(
            @PathVariable("id") String id,
            @Valid @ModelAttribute QuestionnaireCompositeKeyRequest key
    ) {
        DeleteQuestionnaireCommand command = new DeleteQuestionnaireCommand(id, key.channelId(), key.journeyId());
        return deleteQuestionnaireUseCase.execute(command).fold(
                ignored -> ResponseEntity.ok(ApiDataResponse.of(DeleteQuestionnaireResponse.success(id, key.channelId(), key.journeyId()))),
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
    public ResponseEntity<ApiDataResponse<DeleteQuestionnairesResponse>> deleteBatch(@Valid @RequestBody DeleteQuestionnairesRequest request) {
        return deleteQuestionnaireUseCase.execute(request.toCommands()).fold(
                success -> ResponseEntity.ok(ApiDataResponse.of(DeleteQuestionnairesResponse.from(success, request.items().size()))),
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
    public ResponseEntity<ApiDataResponse<QuestionnaireResponse>> getById(
            @PathVariable("id") String id,
            @Valid @ModelAttribute QuestionnaireCompositeKeyRequest key
    ) {
        Optional<QuestionnaireView> found = getQuestionnaireByIdQueryHandler.execute(
                new GetQuestionnaireById(QuestionnaireId.of(id, key.channelId(), key.journeyId()))
        );

        if (found.isEmpty()) {
            throw new DomainResultException(List.of(QuestionnaireErrors.QUESTIONNAIRE_NOT_FOUND.toDomainError(id)));
        }

        return ResponseEntity.ok(ApiDataResponse.of(QuestionnaireResponse.from(found.get())));
    }

    @Override
    @CrossOrigin(
            originPatterns = SEARCH_ALLOWED_ORIGIN_PATTERN,
            allowedHeaders = "*",
            exposedHeaders = {HeaderConstants.CORRELATION_HEADER, HeaderConstants.FLOW_HEADER},
            methods = RequestMethod.GET,
            maxAge = 3600
    )
    public ResponseEntity<ApiCollectionResponse<QuestionnaireResponse>> search(@ModelAttribute SearchQuestionnaireRequest request) {
        PageResult<QuestionnaireView> page = searchQuestionnaireByFilterQueryHandler.execute(request.toQuery());

        List<QuestionnaireResponse> data = page.content().stream()
                .map(QuestionnaireResponse::from)
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

    private <T> ResponseEntity<T> asDomainFailure(List<DomainError> errors) {
        throw new DomainResultException(errors);
    }

    private String nextPageLink(PageResult<QuestionnaireView> page) {
        if (!page.hasNext() || page.page() == null) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQueryParam("mode", PageMode.PAGE.name())
                .replaceQueryParam("page", page.page() + 1)
                .replaceQueryParam("cursor")
                .toUriString();
    }

    private String previousPageLink(PageResult<QuestionnaireView> page) {
        if (page.page() == null || page.page() <= 0) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQueryParam("mode", PageMode.PAGE.name())
                .replaceQueryParam("page", page.page() - 1)
                .replaceQueryParam("cursor")
                .toUriString();
    }

    private String nextCursorLink(PageResult<QuestionnaireView> page) {
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


