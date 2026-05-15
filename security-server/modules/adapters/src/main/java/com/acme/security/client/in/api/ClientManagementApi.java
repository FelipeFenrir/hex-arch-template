package com.acme.security.client.in.api;

import com.acme.security.common.api.ApiCollectionResponse;
import com.acme.security.client.in.dto.request.CreateClientRequest;
import com.acme.security.client.in.dto.request.SearchClientRequest;
import com.acme.security.client.in.dto.response.ClientResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Clients", description = "Client management API")
@RequestMapping("/api/v1/clients")
public interface ClientManagementApi {

    @Operation(summary = "Create client for current tenant")
    @ApiResponse(responseCode = "200", description = "Client created")
    @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping
    ResponseEntity<?> createClient(@RequestBody CreateClientRequest request);

    @Operation(summary = "List clients for current tenant")
    @ApiResponse(responseCode = "200", description = "Clients listed")
    @ApiResponse(
            responseCode = "400",
            description = "Invalid pagination request",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(value = """
                            {
                              "type": "about:blank",
                              "title": "Validation error",
                              "status": 400,
                              "detail": "cursor must not be informed when mode=PAGE"
                            }
                            """)
            )
    )
    @Parameters({
            @Parameter(name = "mode", description = "Pagination mode. Use PAGE or CURSOR.",
                    schema = @Schema(type = "string", allowableValues = {"PAGE", "CURSOR"})),
            @Parameter(name = "page", description = "Page index (0-based). Use only when mode=PAGE."),
            @Parameter(name = "size", description = "Page size for both PAGE and CURSOR modes."),
            @Parameter(name = "cursor", description = "Cursor token. Optional for first CURSOR request; required for next pages."),
            @Parameter(name = "sort", description = "Sort expression(s) in format field[,ASC|DESC]. Example: clientId,ASC")
    })
    @GetMapping
    ResponseEntity<ApiCollectionResponse<ClientResponse>> list(@ModelAttribute SearchClientRequest request);
}

