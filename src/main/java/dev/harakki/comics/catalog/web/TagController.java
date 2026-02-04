package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.TagService;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = TagController.REQUEST_MAPPING, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Tags", description = "Management of comic tags/genres.")
class TagController {

    static final String REQUEST_MAPPING = "/api/v1/tags";

    static final String BY_ID = "/{id}";
    static final String BY_SLUG = "/slug/{slug}";

    private final TagService tagService;

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
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagResponse createTag(@RequestBody @Valid TagCreateRequest request) {
        return tagService.create(request);
    }

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
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(BY_ID)
    public TagResponse updateTag(
            @Parameter(description = "Tag UUID", required = true)
            @PathVariable @NotNull UUID id,
            @RequestBody @Valid TagUpdateRequest request
    ) {
        return tagService.update(id, request);
    }

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
    @GetMapping(BY_ID)
    public TagResponse getTag(
            @Parameter(description = "Tag UUID", required = true)
            @PathVariable @NotNull UUID id
    ) {
        return tagService.getById(id);
    }

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
    @GetMapping(BY_SLUG)
    public TagResponse getTagBySlug(
            @Parameter(description = "URL slug", example = "action", required = true)
            @PathVariable String slug
    ) {
        return tagService.getBySlug(slug);
    }

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
    @GetMapping
    public Page<TagResponse> getAllTags(
            @ParameterObject @PageableDefault(sort = "name") Pageable pageable
    ) {
        return tagService.getAll(pageable);
    }

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
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(BY_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTag(
            @Parameter(description = "Tag UUID", required = true)
            @PathVariable @NotNull UUID id
    ) {
        tagService.delete(id);
    }

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
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(BY_ID + "/slug")
    public TagResponse updateTagSlug(
            @Parameter(description = "Tag UUID", required = true)
            @PathVariable @NotNull UUID id,
            @RequestBody @Valid ReplaceSlugRequest request
    ) {
        return tagService.updateSlug(id, request);
    }

}
