package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.PublisherService;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.LikeIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
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
@RequestMapping(path = PublisherController.REQUEST_MAPPING, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Publishers", description = "Management of publishing houses.")
class PublisherController {

    static final String REQUEST_MAPPING = "/api/v1/publishers";

    static final String BY_ID = "/{id}";
    static final String BY_SLUG = "/slug/{slug}";

    private final PublisherService publisherService;

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
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PublisherResponse createPublisher(@RequestBody @Valid PublisherCreateRequest request) {
        return publisherService.create(request);
    }

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
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(BY_ID)
    public PublisherResponse updatePublisher(
            @Parameter(description = "Publisher UUID", required = true)
            @PathVariable @NotNull UUID id,
            @RequestBody @Valid PublisherUpdateRequest request
    ) {
        return publisherService.update(id, request);
    }

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
    @GetMapping(BY_ID)
    public PublisherResponse getPublisher(
            @Parameter(description = "Publisher UUID", required = true)
            @PathVariable @NotNull UUID id
    ) {
        return publisherService.getById(id);
    }

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
    @GetMapping(BY_SLUG)
    public PublisherResponse getPublisherBySlug(
            @Parameter(description = "URL slug", example = "shueisha", required = true)
            @PathVariable @NotNull String slug
    ) {
        return publisherService.getBySlug(slug);
    }

    @Operation(
            operationId = "searchPublishers",
            summary = "Search and filter publishers",
            description = "Retrieves publishers with optional filtering."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of publishers",
                    content = @Content(schema = @Schema(implementation = PublisherResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest")
    })
    @GetMapping
    @Parameters({
            @Parameter(name = "search", description = "Search by name or slug", example = "shueisha"),
            @Parameter(name = "country", description = "Filter by Country ISO Code", example = "JP")
    })
    public Page<PublisherResponse> getAllPublishers(
            @Or({
                    @Spec(path = "name", params = "search", spec = LikeIgnoreCase.class),
                    @Spec(path = "slug", params = "search", spec = LikeIgnoreCase.class)
            }) @Parameter(hidden = true) Specification<Publisher> searchSpec,
            @And({
                    @Spec(path = "countryIsoCode", params = "country", spec = Equal.class)
            }) @Parameter(hidden = true) Specification<Publisher> filterSpec,
            @ParameterObject @PageableDefault(sort = "name") Pageable pageable
    ) {
        Specification<Publisher> spec = Specification.where(searchSpec).and(filterSpec);
        return publisherService.getAll(spec, pageable);
    }

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
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(BY_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePublisher(
            @Parameter(description = "Publisher UUID", required = true)
            @PathVariable @NotNull UUID id
    ) {
        publisherService.delete(id);
    }

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
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(BY_ID + "/slug")
    public PublisherResponse updatePublisherSlug(
            @Parameter(description = "Publisher UUID", required = true)
            @PathVariable @NotNull UUID id,
            @RequestBody @Valid ReplaceSlugRequest request
    ) {
        return publisherService.updateSlug(id, request);
    }

}
