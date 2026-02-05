package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.domain.Author;
import dev.harakki.comics.catalog.dto.AuthorCreateRequest;
import dev.harakki.comics.catalog.dto.AuthorResponse;
import dev.harakki.comics.catalog.dto.AuthorUpdateRequest;
import dev.harakki.comics.catalog.dto.ReplaceSlugRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

@Tag(name = "Authors", description = "Management of comic creators.")
public interface AuthorApi {

    @Operation(
            operationId = "getAuthorById",
            summary = "Get author by ID",
            description = "Retrieve author details by UUID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Author found",
                    content = @Content(schema = @Schema(implementation = AuthorResponse.class))),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    AuthorResponse getAuthor(@Parameter(description = "Author UUID", required = true) UUID id);

    @Operation(
            operationId = "getAuthorBySlug",
            summary = "Get author by slug",
            description = "SEO-friendly retrieval."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Author found",
                    content = @Content(schema = @Schema(implementation = AuthorResponse.class))),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    AuthorResponse getAuthorBySlug(
            @Parameter(description = "URL slug", example = "tatsuki-fujimoto", required = true) String slug
    );

    @Operation(
            operationId = "searchAuthors",
            summary = "Search and filter authors",
            description = "Retrieves authors with optional filtering."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of authors",
                    content = @Content(schema = @Schema(implementation = AuthorResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest")
    })
    @Parameters({
            @Parameter(name = "search", description = "Search by name or slug", example = "fujimoto"),
            @Parameter(name = "country", description = "Filter by Country ISO Code", example = "JP")
    })
    Page<AuthorResponse> getAllAuthors(@Parameter(hidden = true) Specification<Author> searchSpec,
                                       @Parameter(hidden = true) Specification<Author> filterSpec,
                                       @ParameterObject Pageable pageable);

    @Operation(
            operationId = "createAuthor",
            summary = "Create author",
            description = "Add a new author."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Author created successfully",
                    content = @Content(schema = @Schema(implementation = AuthorResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "409", ref = "Conflict")
    })
    AuthorResponse createAuthor(AuthorCreateRequest request);

    @Operation(
            operationId = "updateAuthor",
            summary = "Update author",
            description = "Update author personal details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Author updated",
                    content = @Content(schema = @Schema(implementation = AuthorResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    AuthorResponse updateAuthor(@Parameter(description = "Author UUID", required = true) UUID id,
                                AuthorUpdateRequest request);

    @Operation(
            operationId = "updateAuthorSlug",
            summary = "Update slug",
            description = "Manually change the URL slug."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slug updated",
                    content = @Content(schema = @Schema(implementation = AuthorResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "409", ref = "Conflict")
    })
    AuthorResponse updateAuthorSlug(
            @Parameter(description = "Author UUID", required = true) UUID id, ReplaceSlugRequest request
    );

    @Operation(
            operationId = "deleteAuthor",
            summary = "Delete author",
            description = "Delete author entry."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Author deleted"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    void deleteAuthor(@Parameter(description = "Author UUID", required = true) UUID id);

}
