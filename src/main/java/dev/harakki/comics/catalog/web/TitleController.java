package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.TitleService;
import dev.harakki.comics.catalog.domain.Title;
import dev.harakki.comics.catalog.domain.Title_;
import dev.harakki.comics.catalog.dto.*;
import dev.harakki.comics.shared.api.ApiProblemResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.criteria.JoinType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.kaczmarzyk.spring.data.jpa.domain.*;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/titles", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Titles", description = "Management of comic titles")
@ApiProblemResponses
class TitleController {

    private final TitleService titleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create title", description = "Create a new comic entry.")
    @ApiResponse(responseCode = "201", description = "Title created successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TitleResponse.class)))
    public TitleResponse createTitle(@RequestBody @Valid TitleCreateRequest request) {
        return titleService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update title", description = "Update main metadata.")
    @ApiResponse(responseCode = "200", description = "Title updated")
    public TitleResponse updateTitle(
            @PathVariable @NotNull UUID id,
            @RequestBody @Valid TitleUpdateRequest request
    ) {
        return titleService.update(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get title by ID", description = "Retrieve full details.")
    @ApiResponse(responseCode = "200", description = "Title found")
    public TitleResponse getTitle(@PathVariable @NotNull UUID id) {
        return titleService.getById(id);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get title by slug", description = "SEO-friendly retrieval.")
    @ApiResponse(responseCode = "200", description = "Title found")
    public TitleResponse getTitleBySlug(
            @Parameter(description = "URL slug", example = "chainsaw-man")
            @PathVariable @NotNull String slug
    ) {
        return titleService.getBySlug(slug);
    }

    @GetMapping
    @Operation(summary = "Search and filter titles", description = "Retrieves titles with optional filtering.")
    @Parameters({
            @Parameter(name = "search", description = "Search text", example = "chainsaw man"),
            @Parameter(name = "type", description = "Filter by type", example = "MANGA"),
            @Parameter(name = "titleStatus", description = "Filter by status", example = "COMPLETED"),
            @Parameter(name = "country", description = "Filter by Country ISO Code", example = "JP"),
            @Parameter(name = "tags", description = "Filter by tag slugs", example = "action,shonen"),
            @Parameter(name = "releaseYear", description = "Release year", example = "2018"),
            @Parameter(name = "yearFrom", description = "Min release year", example = "2000"),
            @Parameter(name = "yearTo", description = "Max release year", example = "2020"),
            @Parameter(name = "contentRating", description = "Max content rating", example = "EROTICA")
    })
    public Page<TitleResponse> getAllTitles(
            @Or({
                    @Spec(path = Title_.NAME, params = "search", spec = LikeIgnoreCase.class),
                    @Spec(path = Title_.SLUG, params = "search", spec = LikeIgnoreCase.class)
            }) @Parameter(hidden = true) Specification<Title> searchSpec,
            @Join(path = Title_.TAGS, alias = "t", type = JoinType.LEFT)
            @And({
                    @Spec(path = Title_.TYPE, spec = In.class),
                    @Spec(path = Title_.TITLE_STATUS, spec = In.class),
                    @Spec(path = Title_.COUNTRY_ISO_CODE, params = "country", spec = Equal.class),
                    @Spec(path = Title_.RELEASE_YEAR, params = "releaseYear", spec = Equal.class),
                    @Spec(path = Title_.RELEASE_YEAR, params = "yearFrom", spec = GreaterThanOrEqual.class),
                    @Spec(path = Title_.RELEASE_YEAR, params = "yearTo", spec = LessThanOrEqual.class),
                    @Spec(path = Title_.CONTENT_RATING, params = "contentRating", spec = LessThanOrEqual.class),
                    @Spec(path = "t.slug", params = "tags", spec = In.class)
            }) @Parameter(hidden = true) Specification<Title> filterSpec,

            @ParameterObject @PageableDefault(sort = Title_.NAME) Pageable pageable
    ) {
        Specification<Title> spec = Specification.where(searchSpec).and(filterSpec);
        return titleService.getAll(spec, pageable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete title", description = "Delete title entry.")
    @ApiResponse(responseCode = "204", description = "Title deleted")
    public void deleteTitle(@PathVariable @NotNull UUID id) {
        titleService.delete(id);
    }

    @PutMapping("/{id}/slug")
    @Operation(summary = "Update slug", description = "Update URL slug.")
    public TitleResponse updateTitleSlug(
            @PathVariable @NotNull UUID id,
            @RequestBody @Valid ReplaceSlugRequest request
    ) {
        return titleService.updateSlug(id, request);
    }

    @PostMapping("/{id}/authors")
    @Operation(summary = "Add author", description = "Link an author to the title.")
    @ApiResponse(responseCode = "200", description = "Author added")
    public TitleResponse addAuthor(
            @PathVariable @NotNull UUID id,
            @RequestBody @Valid TitleAddAuthorRequest request
    ) {
        return titleService.addAuthor(id, request.authorId(), request.role());
    }

    @DeleteMapping("/{id}/authors/{authorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove author", description = "Unlink an author from the title.")
    public TitleResponse removeAuthor(
            @PathVariable @NotNull UUID id,
            @PathVariable @NotNull UUID authorId
    ) {
        return titleService.removeAuthor(id, authorId);
    }

    @DeleteMapping("/{id}/publisher")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove publisher", description = "Unlink the publisher from the title.")
    public TitleResponse removePublisher(@PathVariable @NotNull UUID id) {
        return titleService.removePublisher(id);
    }

    @PostMapping("/{id}/tags")
    @Operation(summary = "Replace tags", description = "Fully replace the set of tags.")
    public TitleResponse updateTags(
            @PathVariable @NotNull UUID id,
            @RequestBody @Valid ReplaceTagsRequest request
    ) {
        return titleService.updateTags(id, request);
    }

}
