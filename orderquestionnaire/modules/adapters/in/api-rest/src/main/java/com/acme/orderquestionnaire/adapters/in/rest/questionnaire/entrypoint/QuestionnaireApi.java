package com.acme.orderquestionnaire.adapters.in.rest.questionnaire.entrypoint;

import com.acme.orderquestionnaire.adapters.in.rest.common.ApiCollectionResponse;
import com.acme.orderquestionnaire.adapters.in.rest.common.ApiDataResponse;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request.CreateQuestionnaireRequest;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request.DeleteQuestionnairesRequest;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request.QuestionnaireCompositeKeyRequest;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request.SearchQuestionnaireRequest;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request.UpdateQuestionnaireRequest;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.request.ValidateQuestionnaireAnswersRequest;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.response.DeleteQuestionnaireResponse;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.response.DeleteQuestionnairesResponse;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.response.QuestionnaireResponse;
import com.acme.orderquestionnaire.adapters.in.rest.questionnaire.response.ValidateQuestionnaireAnswersResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Questionnaire", description = "Questionnaire management API")
@RequestMapping("/api/v1/questionnaires")
public interface QuestionnaireApi {

    @Operation(summary = "Create a questionnaire")
    @ApiResponse(responseCode = "200", description = "Questionnaire created")
    @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(value = """
                            {
                              "type": "about:blank",
                              "title": "Validation error",
                              "status": 400,
                              "code": "VAL-001",
                              "detail": "One or more fields are invalid.",
                              "instance": "/api/v1/questionnaires"
                            }
                            """)
            )
    )
    @PostMapping
    ResponseEntity<ApiDataResponse<QuestionnaireResponse>> create(@Valid @RequestBody CreateQuestionnaireRequest request);

    @Operation(summary = "Update a questionnaire")
    @ApiResponse(responseCode = "200", description = "Questionnaire updated")
    @PutMapping("/{id}")
    ResponseEntity<ApiDataResponse<QuestionnaireResponse>> update(
            @PathVariable("id") String id,
            @Valid @ModelAttribute QuestionnaireCompositeKeyRequest key,
            @Valid @RequestBody UpdateQuestionnaireRequest request
    );

    @Operation(summary = "Delete a questionnaire by composite key")
    @ApiResponse(responseCode = "200", description = "Questionnaire deleted")
    @DeleteMapping("/{id}")
    ResponseEntity<ApiDataResponse<DeleteQuestionnaireResponse>> deleteById(
            @PathVariable("id") String id,
            @Valid @ModelAttribute QuestionnaireCompositeKeyRequest key
    );

    @Operation(summary = "Delete questionnaires in batch")
    @ApiResponse(responseCode = "200", description = "Batch delete processed")
    @DeleteMapping
    ResponseEntity<ApiDataResponse<DeleteQuestionnairesResponse>> deleteBatch(@Valid @RequestBody DeleteQuestionnairesRequest request);

    @Operation(summary = "Get questionnaire by composite key")
    @ApiResponse(responseCode = "200", description = "Questionnaire found")
    @ApiResponse(
            responseCode = "404",
            description = "Questionnaire not found",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
    )
    @GetMapping("/{id}")
    ResponseEntity<ApiDataResponse<QuestionnaireResponse>> getById(
            @PathVariable("id") String id,
            @Valid @ModelAttribute QuestionnaireCompositeKeyRequest key
    );

    @Operation(summary = "Validate questionnaire answers")
    @ApiResponse(responseCode = "200", description = "Validation completed (returns valid=true or valid=false with violations)")
    @ApiResponse(
            responseCode = "400",
            description = "Validation error in request payload",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Questionnaire not found",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
    )
    @PostMapping("/validate-answers")
    ResponseEntity<ApiDataResponse<ValidateQuestionnaireAnswersResponse>> validateAnswers(
            @Valid @RequestBody ValidateQuestionnaireAnswersRequest request
    );

    @Operation(summary = "Search questionnaires with page or cursor pagination")
    @ApiResponse(responseCode = "200", description = "Search result")
    @Parameters({
            @Parameter(name = "mode", description = "Pagination mode. Use PAGE or CURSOR. Defaults to PAGE when omitted.",
                    schema = @Schema(type = "string", allowableValues = {"PAGE", "CURSOR"})),
            @Parameter(name = "page", description = "Page index (0-based). Use only when mode=PAGE."),
            @Parameter(name = "size", description = "Page size for both PAGE and CURSOR modes."),
            @Parameter(name = "cursor", description = "Cursor token. Optional for first CURSOR request; required for next pages.")
    })
    @GetMapping
    ResponseEntity<ApiCollectionResponse<QuestionnaireResponse>> search(@ModelAttribute SearchQuestionnaireRequest request);
}


