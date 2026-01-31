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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/tags", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Tags", description = "Management of comic tags/genres.")
class TagController {

    private final TagService tagService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
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
    public TagResponse createTag(@RequestBody @Valid TagCreateRequest request) {
        return tagService.create(request);
    }

    @PutMapping("/{id}")
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
    public TagResponse updateTag(
            @Parameter(description = "Tag UUID", required = true)
            @PathVariable @NotNull UUID id,
            @RequestBody @Valid TagUpdateRequest request
    ) {
        return tagService.update(id, request);
    }

    @GetMapping("/{id}")
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
    public TagResponse getTag(
            @Parameter(description = "Tag UUID", required = true)
            @PathVariable @NotNull UUID id
    ) {
        return tagService.getById(id);
    }

    @GetMapping("/slug/{slug}")
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
            @Parameter(description = "URL slug", example = "action", required = true)
            @PathVariable String slug
    ) {
        return tagService.getBySlug(slug);
    }

    @GetMapping
    @Operation(
            operationId = "getAllTags",
            summary = "Get all tags",
            description = "Paginated list of tags."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of tags",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest")
    })
    public Page<TagResponse> getAllTags(
            @ParameterObject @PageableDefault(sort = "name") Pageable pageable
    ) {
        return tagService.getAll(pageable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
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
    public void deleteTag(
            @Parameter(description = "Tag UUID", required = true)
            @PathVariable @NotNull UUID id
    ) {
        tagService.delete(id);
    }

    @PutMapping("/{id}/slug")
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
    public TagResponse updateTagSlug(
            @Parameter(description = "Tag UUID", required = true)
            @PathVariable @NotNull UUID id,
            @RequestBody @Valid ReplaceSlugRequest request
    ) {
        return tagService.updateSlug(id, request);
    }

}
