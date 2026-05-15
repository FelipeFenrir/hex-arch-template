package com.acme.security.user.in.api;

import com.acme.security.common.api.ApiCollectionResponse;
import com.acme.security.user.in.dto.request.CreateUserRequest;
import com.acme.security.user.in.dto.request.SearchUserRequest;
import com.acme.security.user.in.dto.request.UpdateUserRequest;
import com.acme.security.user.in.dto.response.UserResponse;
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

@Tag(name = "Users", description = "User management API")
@RequestMapping("/api/v1/users")
public interface UserManagementApi {

    @Operation(summary = "Create user for current tenant")
    @ApiResponse(responseCode = "201", description = "User created")
    @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping
    ResponseEntity<?> create(@RequestBody CreateUserRequest request);

    @Operation(summary = "Get user by id")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{id}")
    ResponseEntity<?> findById(@PathVariable("id") String id);

    @Operation(summary = "List users for current tenant")
    @ApiResponse(responseCode = "200", description = "Users listed")
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
                              "detail": "page must not be informed when mode=CURSOR"
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
            @Parameter(name = "sort", description = "Sort expression(s) in format field[,ASC|DESC]. Example: username,DESC")
    })
    @GetMapping
    ResponseEntity<ApiCollectionResponse<UserResponse>> list(@ModelAttribute SearchUserRequest request);

    @Operation(summary = "Update user")
    @ApiResponse(responseCode = "200", description = "User updated")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PutMapping("/{id}")
    ResponseEntity<?> update(@PathVariable("id") String id, @RequestBody UpdateUserRequest request);

    @Operation(summary = "Delete user")
    @ApiResponse(responseCode = "204", description = "User deleted")
    @ApiResponse(responseCode = "404", description = "User not found")
    @DeleteMapping("/{id}")
    ResponseEntity<?> delete(@PathVariable("id") String id);
}

