package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.TagService;
import dev.harakki.comics.catalog.dto.TagCreateRequest;
import dev.harakki.comics.catalog.dto.TagResponse;
import dev.harakki.comics.catalog.dto.TagUpdateRequest;
import dev.harakki.comics.shared.api.ApiProblemResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
@Tag(name = "Tags")
@ApiProblemResponses
class TagController {

    private final TagService tagService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create tag", description = "Create a new tag.")
    @ApiResponse(responseCode = "201", description = "Tag created", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TagResponse.class)))
    public TagResponse createTag(@RequestBody @Valid TagCreateRequest request) {
        return tagService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update tag", description = "Update tag details.")
    @ApiResponse(responseCode = "200", description = "Tag updated")
    public TagResponse updateTag(
            @PathVariable @NotNull UUID id,
            @RequestBody @Valid TagUpdateRequest request
    ) {
        return tagService.update(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tag by ID")
    @ApiResponse(responseCode = "200", description = "Tag found")
    public TagResponse getTag(@PathVariable @NotNull UUID id) {
        return tagService.getById(id);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get tag by slug")
    @ApiResponse(responseCode = "200", description = "Tag found")
    public TagResponse getTagBySlug(
            @Parameter(description = "URL slug", example = "shonen")
            @PathVariable String slug
    ) {
        return tagService.getBySlug(slug);
    }

    @GetMapping
    @Operation(summary = "Get all tags", description = "Paginated list of tags.")
    public Page<TagResponse> getAllTags(
            @ParameterObject @PageableDefault(sort = "name") Pageable pageable
    ) {
        return tagService.getAll(pageable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete tag", description = "Delete tag entry.")
    @ApiResponse(responseCode = "204", description = "Tag deleted")
    public void deleteTag(@PathVariable @NotNull UUID id) {
        tagService.delete(id);
    }

    @PutMapping("/{id}/slug")
    @Operation(summary = "Update slug")
    public TagResponse updateTagSlug(
            @PathVariable @NotNull UUID id,
            @RequestBody @NotBlank @Parameter(description = "New slug", example = "shounen") String slug
    ) {
        return tagService.updateSlug(id, slug);
    }

}
