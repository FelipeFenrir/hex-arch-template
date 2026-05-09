package com.acme.orderquestionnaire.adapters.in.rest.question.entrypoint;

import com.acme.orderquestionnaire.adapters.in.rest.common.ApiCollectionResponse;
import com.acme.orderquestionnaire.adapters.in.rest.common.ApiDataResponse;
import com.acme.orderquestionnaire.adapters.in.rest.question.request.CreateQuestionRequest;
import com.acme.orderquestionnaire.adapters.in.rest.question.request.DeleteQuestionsRequest;
import com.acme.orderquestionnaire.adapters.in.rest.question.request.SearchQuestionRequest;
import com.acme.orderquestionnaire.adapters.in.rest.question.request.UpdateQuestionRequest;
import com.acme.orderquestionnaire.adapters.in.rest.question.response.CreateQuestionResponse;
import com.acme.orderquestionnaire.adapters.in.rest.question.response.DeleteQuestionResponse;
import com.acme.orderquestionnaire.adapters.in.rest.question.response.DeleteQuestionsResponse;
import com.acme.orderquestionnaire.adapters.in.rest.question.response.QuestionResponse;
import com.acme.orderquestionnaire.adapters.in.rest.question.response.UpdateQuestionResponse;
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
import org.springframework.web.bind.annotation.*;

@Tag(name = "Question", description = "Question management API")
@RequestMapping("/api/v1/questions")
public interface QuestionApi {

    @Operation(summary = "Create a question")
    @ApiResponse(responseCode = "200", description = "Question created")
    @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(value = """
                            {
                              \"type\": \"about:blank\",
                              \"title\": \"Validation error\",
                              \"status\": 400,
                              \"code\": \"VAL-001\",
                              \"detail\": \"One or more fields are invalid.\",
                              \"instance\": \"/api/v1/questions\",
                              \"errors\": [{\"field\": \"label\", \"message\": \"must not be blank\"}]
                            }
                            """)
            )
    )
    @PostMapping
    ResponseEntity<ApiDataResponse<CreateQuestionResponse>> create(@Valid @RequestBody CreateQuestionRequest request);

    @Operation(summary = "Update a question")
    @ApiResponse(responseCode = "200", description = "Question updated")
    @ApiResponse(
            responseCode = "409",
            description = "Business conflict",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(value = """
                            {
                              \"type\": \"about:blank\",
                              \"title\": \"Business rule violation\",
                              \"status\": 409,
                              \"code\": \"DOM-INVALID_STATUS_TRANSITION\",
                              \"detail\": \"Cannot transition from 'DRAFT' to 'DRAFT'\",
                              \"instance\": \"/api/v1/questions/q1\"
                            }
                            """)
            )
    )
    @PutMapping("/{id}")
    ResponseEntity<ApiDataResponse<UpdateQuestionResponse>> update(@PathVariable("id") String id,
                                                                   @Valid @RequestBody UpdateQuestionRequest request);

    @Operation(summary = "Delete a question by id")
    @ApiResponse(responseCode = "200", description = "Question deleted")
    @ApiResponse(
            responseCode = "404",
            description = "Question not found",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(value = """
                            {
                              \"type\": \"about:blank\",
                              \"title\": \"Resource not found\",
                              \"status\": 404,
                              \"code\": \"DOM-QUESTION_NOT_FOUND\",
                              \"detail\": \"question was not found\",
                              \"instance\": \"/api/v1/questions/missing\"
                            }
                            """)
            )
    )
    @DeleteMapping("/{id}")
    ResponseEntity<ApiDataResponse<DeleteQuestionResponse>> deleteById(@PathVariable("id") String id);

    @Operation(summary = "Delete questions in batch")
    @ApiResponse(responseCode = "200", description = "Batch delete processed")
    @DeleteMapping
    ResponseEntity<ApiDataResponse<DeleteQuestionsResponse>> deleteBatch(@Valid @RequestBody DeleteQuestionsRequest request);

    @Operation(summary = "Get question by id")
    @ApiResponse(responseCode = "200", description = "Question found")
    @ApiResponse(
            responseCode = "404",
            description = "Question not found",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
    )
    @GetMapping("/{id}")
    ResponseEntity<ApiDataResponse<QuestionResponse>> getById(@PathVariable("id") String id);

    @Operation(summary = "Search questions with page or cursor pagination")
    @ApiResponse(responseCode = "200", description = "Search result")
    @ApiResponse(
            responseCode = "500",
            description = "Unexpected server error",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(value = """
                            {
                              \"type\": \"about:blank\",
                              \"title\": \"Internal server error\",
                              \"status\": 500,
                              \"code\": \"SYS-001\",
                              \"detail\": \"An unexpected internal error occurred.\",
                              \"instance\": \"/api/v1/questions\"
                            }
                            """)
            )
    )
    @Parameters({
            @Parameter(name = "mode", description = "Pagination mode. Use PAGE or CURSOR. Defaults to PAGE when omitted.",
                    schema = @Schema(type = "string", allowableValues = {"PAGE", "CURSOR"})),
            @Parameter(name = "page", description = "Page index (0-based). Use only when mode=PAGE."),
            @Parameter(name = "size", description = "Page size for both PAGE and CURSOR modes."),
            @Parameter(name = "cursor", description = "Cursor token. Optional for first CURSOR request; required for next pages.")
    })
    @GetMapping
    ResponseEntity<ApiCollectionResponse<QuestionResponse>> search(@ModelAttribute SearchQuestionRequest request);
}

