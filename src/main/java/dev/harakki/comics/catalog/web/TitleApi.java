package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.domain.Title;
import dev.harakki.comics.catalog.dto.*;
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

@Tag(name = "Titles", description = "Management of comic titles")
public interface TitleApi {

    @Operation(
            operationId = "createTitle",
            summary = "Create title",
            description = "Create a new comic entry."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Title created successfully",
                    content = @Content(schema = @Schema(implementation = TitleResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "409", ref = "Conflict")
    })
    TitleResponse createTitle(TitleCreateRequest request);

    @Operation(
            operationId = "updateTitle",
            summary = "Update title",
            description = "Update main metadata."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Title updated",
                    content = @Content(schema = @Schema(implementation = TitleResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    TitleResponse updateTitle(
            @Parameter(description = "Title UUID", required = true) UUID id,
            TitleUpdateRequest request
    );

    @Operation(
            operationId = "getTitleById",
            summary = "Get title by ID",
            description = "Retrieve full details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Title found",
                    content = @Content(schema = @Schema(implementation = TitleResponse.class))),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    TitleResponse getTitle(@Parameter(description = "Title UUID", required = true) UUID id);

    @Operation(
            operationId = "getTitleBySlug",
            summary = "Get title by slug",
            description = "SEO-friendly retrieval."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Title found",
                    content = @Content(schema = @Schema(implementation = TitleResponse.class))),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    TitleResponse getTitleBySlug(
            @Parameter(description = "URL slug", example = "chainsaw-man", required = true) String slug
    );

    @Operation(
            operationId = "searchTitles",
            summary = "Search and filter titles",
            description = "Retrieves titles with optional filtering."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of titles",
                    content = @Content(schema = @Schema(implementation = TitleResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest")
    })
    @Parameters({
            @Parameter(name = "search", description = "Search text", example = "chainsaw man"),
            @Parameter(name = "type", description = "Filter by type", example = "MANGA"),
            @Parameter(name = "titleStatus", description = "Filter by status", example = "COMPLETED"),
            @Parameter(name = "country", description = "Filter by Country ISO Code", example = "JP"),
            @Parameter(name = "tags", description = "Filter by tag slugs", example = "action,shonen"),
            @Parameter(name = "releaseYear", description = "Release year", example = "2018"),
            @Parameter(name = "yearFrom", description = "Min release year", example = "2000"),
            @Parameter(name = "yearTo", description = "Max release year", example = "2020"),
            @Parameter(name = "contentRating", description = "Max content rating", example = "EIGHTEEN_PLUS")
    })
    Page<TitleResponse> getAllTitles(
            @Parameter(hidden = true) Specification<Title> searchSpec,
            @Parameter(hidden = true) Specification<Title> filterSpec,
            @ParameterObject Pageable pageable
    );

    @Operation(
            operationId = "deleteTitle",
            summary = "Delete title",
            description = "Delete title entry."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Title deleted"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    void deleteTitle(@Parameter(description = "Title UUID", required = true) UUID id);

    @Operation(
            operationId = "updateTitleSlug",
            summary = "Update slug",
            description = "Update URL slug."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slug updated",
                    content = @Content(schema = @Schema(implementation = TitleResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "409", ref = "Conflict")
    })
    TitleResponse updateTitleSlug(
            @Parameter(description = "Title UUID", required = true) UUID id,
            ReplaceSlugRequest request
    );

    @Operation(
            operationId = "addAuthorToTitle",
            summary = "Add author",
            description = "Link an author to the title."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Author added",
                    content = @Content(schema = @Schema(implementation = TitleResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    TitleResponse addAuthor(
            @Parameter(description = "Title UUID", required = true) UUID id,
            TitleAddAuthorRequest request
    );

    @Operation(
            operationId = "removeAuthorFromTitle",
            summary = "Remove author",
            description = "Unlink an author from the title."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Author removed",
                    content = @Content(schema = @Schema(implementation = TitleResponse.class))),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    TitleResponse removeAuthor(
            @Parameter(description = "Title UUID", required = true) UUID id,
            @Parameter(description = "Author UUID", required = true) UUID authorId
    );

    @Operation(
            operationId = "removePublisherFromTitle",
            summary = "Remove publisher",
            description = "Unlink the publisher from the title."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publisher removed",
                    content = @Content(schema = @Schema(implementation = TitleResponse.class))),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    TitleResponse removePublisher(
            @Parameter(description = "Title UUID", required = true) UUID id
    );

    @Operation(
            operationId = "replaceTitleTags",
            summary = "Replace tags",
            description = "Fully replace the set of tags."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tags replaced",
                    content = @Content(schema = @Schema(implementation = TitleResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    TitleResponse updateTags(
            @Parameter(description = "Title UUID", required = true) UUID id,
            ReplaceTagsRequest request
    );

}
