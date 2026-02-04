package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.TitleService;
import dev.harakki.comics.catalog.domain.Title;
import dev.harakki.comics.catalog.domain.Title_;
import dev.harakki.comics.catalog.dto.*;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = TitleController.REQUEST_MAPPING, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Titles", description = "Management of comic titles")
class TitleController {

    static final String REQUEST_MAPPING = "/api/v1/titles";

    static final String BY_ID = "/{id}";
    static final String BY_SLUG = "/slug/{slug}";

    private final TitleService titleService;

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
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TitleResponse createTitle(@RequestBody @Valid TitleCreateRequest request) {
        return titleService.create(request);
    }

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
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(BY_ID)
    public TitleResponse updateTitle(
            @Parameter(description = "Title UUID", required = true)
            @PathVariable @NotNull UUID id,
            @RequestBody @Valid TitleUpdateRequest request
    ) {
        return titleService.update(id, request);
    }

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
    @GetMapping(BY_ID)
    public TitleResponse getTitle(
            @Parameter(description = "Title UUID", required = true)
            @PathVariable @NotNull UUID id
    ) {
        return titleService.getById(id);
    }

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
    @GetMapping(BY_SLUG)
    public TitleResponse getTitleBySlug(
            @Parameter(description = "URL slug", example = "chainsaw-man", required = true)
            @PathVariable @NotNull String slug
    ) {
        return titleService.getBySlug(slug);
    }

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
    @GetMapping
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
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(BY_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTitle(
            @Parameter(description = "Title UUID", required = true)
            @PathVariable @NotNull UUID id
    ) {
        titleService.delete(id);
    }

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
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(BY_ID + "/slug")
    public TitleResponse updateTitleSlug(
            @Parameter(description = "Title UUID", required = true)
            @PathVariable @NotNull UUID id,
            @RequestBody @Valid ReplaceSlugRequest request
    ) {
        return titleService.updateSlug(id, request);
    }

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
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(BY_ID + "/authors")
    public TitleResponse addAuthor(
            @Parameter(description = "Title UUID", required = true)
            @PathVariable @NotNull UUID id,
            @RequestBody @Valid TitleAddAuthorRequest request
    ) {
        return titleService.addAuthor(id, request.authorId(), request.role());
    }

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
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(BY_ID + "/authors/{authorId}")
    public TitleResponse removeAuthor(
            @Parameter(description = "Title UUID", required = true)
            @PathVariable @NotNull UUID id,
            @Parameter(description = "Author UUID", required = true)
            @PathVariable @NotNull UUID authorId
    ) {
        return titleService.removeAuthor(id, authorId);
    }

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
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(BY_ID + "/publisher")
    public TitleResponse removePublisher(
            @Parameter(description = "Title UUID", required = true)
            @PathVariable @NotNull UUID id
    ) {
        return titleService.removePublisher(id);
    }

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
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(BY_ID + "/tags")
    public TitleResponse updateTags(
            @Parameter(description = "Title UUID", required = true)
            @PathVariable @NotNull UUID id,
            @RequestBody @Valid ReplaceTagsRequest request
    ) {
        return titleService.updateTags(id, request);
    }

}
