package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.domain.Publisher;
import dev.harakki.comics.catalog.dto.PublisherCreateRequest;
import dev.harakki.comics.catalog.dto.PublisherResponse;
import dev.harakki.comics.catalog.dto.PublisherUpdateRequest;
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

@Tag(name = "Publishers", description = "Management of publishing houses.")
public interface PublisherApi {

    @Operation(
            operationId = "getPublisherById",
            summary = "Get publisher by ID",
            description = "Retrieve publisher by UUID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publisher found",
                    content = @Content(schema = @Schema(implementation = PublisherResponse.class))),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    PublisherResponse getPublisher(
            @Parameter(description = "Publisher UUID", required = true) UUID id
    );

    @Operation(
            operationId = "getPublisherBySlug",
            summary = "Get publisher by slug",
            description = "SEO-friendly retrieval."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publisher found",
                    content = @Content(schema = @Schema(implementation = PublisherResponse.class))),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    PublisherResponse getPublisherBySlug(
            @Parameter(description = "URL slug", example = "shueisha", required = true) String slug
    );

    @Operation(
            operationId = "searchPublishers",
            summary = "Search and filter publishers",
            description = "Retrieves publishers with optional filtering."
    )
    @Parameters({
            @Parameter(name = "search", description = "Search by name or slug", example = "shueisha"),
            @Parameter(name = "country", description = "Filter by Country ISO Code", example = "JP")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of publishers",
                    content = @Content(schema = @Schema(implementation = PublisherResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest")
    })
    Page<PublisherResponse> getAllPublishers(
            @Parameter(hidden = true) Specification<Publisher> searchSpec,
            @Parameter(hidden = true) Specification<Publisher> filterSpec,
            @ParameterObject Pageable pageable
    );

    @Operation(
            operationId = "createPublisher",
            summary = "Create publisher",
            description = "Add a new publisher."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Publisher created",
                    content = @Content(schema = @Schema(implementation = PublisherResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "409", ref = "Conflict")
    })
    PublisherResponse createPublisher(PublisherCreateRequest request);

    @Operation(
            operationId = "updatePublisher",
            summary = "Update publisher",
            description = "Update publisher details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publisher updated",
                    content = @Content(schema = @Schema(implementation = PublisherResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    PublisherResponse updatePublisher(
            @Parameter(description = "Publisher UUID", required = true) UUID id, PublisherUpdateRequest request);

    @Operation(
            operationId = "updatePublisherSlug",
            summary = "Update slug",
            description = "Manually change the URL slug."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slug updated",
                    content = @Content(schema = @Schema(implementation = PublisherResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "409", ref = "Conflict")
    })
    PublisherResponse updatePublisherSlug(
            @Parameter(description = "Publisher UUID", required = true) UUID id,
            ReplaceSlugRequest request
    );

    @Operation(
            operationId = "deletePublisher",
            summary = "Delete publisher",
            description = "Delete publisher entry."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Publisher deleted"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    void deletePublisher(
            @Parameter(description = "Publisher UUID", required = true) UUID id
    );

}
