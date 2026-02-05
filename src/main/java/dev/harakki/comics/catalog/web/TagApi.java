package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.dto.ReplaceSlugRequest;
import dev.harakki.comics.catalog.dto.TagCreateRequest;
import dev.harakki.comics.catalog.dto.TagResponse;
import dev.harakki.comics.catalog.dto.TagUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Tag(name = "Tags", description = "Management of comic tags/genres.")
public interface TagApi {

    @Operation(
            operationId = "createTag",
            summary = "Create tag",
            description = "Create a new tag."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tag created",
                    content = @Content(schema = @Schema(implementation = TagResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "409", ref = "Conflict")
    })
    public TagResponse createTag(TagCreateRequest request);

    @Operation(
            operationId = "updateTag",
            summary = "Update tag",
            description = "Update tag details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag updated",
                    content = @Content(schema = @Schema(implementation = TagResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public TagResponse updateTag(@Parameter(description = "Tag UUID", required = true) UUID id,
                                 TagUpdateRequest request);

    @Operation(
            operationId = "getTagById",
            summary = "Get tag by ID",
            description = "Retrieve tag by UUID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag found",
                    content = @Content(schema = @Schema(implementation = TagResponse.class))),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public TagResponse getTag(@Parameter(description = "Tag UUID", required = true) UUID id);

    @Operation(
            operationId = "getTagBySlug",
            summary = "Get tag by slug",
            description = "SEO-friendly retrieval."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag found",
                    content = @Content(schema = @Schema(implementation = TagResponse.class))),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public TagResponse getTagBySlug(
            @Parameter(description = "URL slug", example = "action", required = true) String slug
    );

    @Operation(
            operationId = "getAllTags",
            summary = "Get all tags",
            description = "Paginated list of tags."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of tags",
                    content = @Content(schema = @Schema(implementation = TagResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest")
    })
    public Page<TagResponse> getAllTags(@ParameterObject Pageable pageable);

    @Operation(
            operationId = "deleteTag",
            summary = "Delete tag",
            description = "Delete tag entry."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tag deleted"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public void deleteTag(@Parameter(description = "Tag UUID", required = true) UUID id);

    @Operation(
            operationId = "updateTagSlug",
            summary = "Update slug",
            description = "Manually change the URL slug."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slug updated",
                    content = @Content(schema = @Schema(implementation = TagResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "409", ref = "Conflict")
    })
    public TagResponse updateTagSlug(@Parameter(description = "Tag UUID", required = true) UUID id,
                                     ReplaceSlugRequest request);


}
