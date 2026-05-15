package com.acme.security.tenant.in.api;

import com.acme.security.common.api.ApiCollectionResponse;
import com.acme.security.tenant.in.dto.request.CreateTenantRequest;
import com.acme.security.tenant.in.dto.request.SearchTenantRequest;
import com.acme.security.tenant.in.dto.request.UpdateTenantRequest;
import com.acme.security.tenant.in.dto.response.TenantResponse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Tag(name = "Tenants", description = "Tenant management API")
@RequestMapping("/api/v1/tenants")
public interface TenantManagementApi {

    @Operation(summary = "Create tenant (admin or super-admin token)")
    @ApiResponse(responseCode = "201", description = "Tenant created")
    @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping
    ResponseEntity<?> create(@RequestBody CreateTenantRequest request);

    @Operation(summary = "List tenants (admin or super-admin token)")
    @ApiResponse(responseCode = "200", description = "Tenants listed")
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
            @Parameter(name = "sort", description = "Sort expression(s) in format field[,ASC|DESC]. Example: name,ASC")
    })
    @GetMapping
    ResponseEntity<ApiCollectionResponse<TenantResponse>> list(@ModelAttribute SearchTenantRequest request);

    @Operation(summary = "Get tenant by id (admin or super-admin token)")
    @ApiResponse(responseCode = "200", description = "Tenant found")
    @ApiResponse(responseCode = "404", description = "Tenant not found")
    @GetMapping("/{id}")
    ResponseEntity<?> getById(@PathVariable("id") String id);

    @Operation(summary = "Update tenant (admin or super-admin token)")
    @ApiResponse(responseCode = "200", description = "Tenant updated")
    @ApiResponse(responseCode = "404", description = "Tenant not found")
    @PutMapping("/{id}")
    ResponseEntity<?> update(@PathVariable("id") String id, @RequestBody UpdateTenantRequest request);

    @Operation(summary = "Delete tenant (super-admin token)")
    @ApiResponse(responseCode = "204", description = "Tenant deleted")
    @ApiResponse(responseCode = "404", description = "Tenant not found")
    @DeleteMapping("/{id}")
    ResponseEntity<?> delete(@PathVariable("id") String id);
}

