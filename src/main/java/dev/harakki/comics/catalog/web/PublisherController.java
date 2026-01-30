package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.PublisherService;
import dev.harakki.comics.catalog.domain.Publisher;
import dev.harakki.comics.catalog.dto.PublisherCreateRequest;
import dev.harakki.comics.catalog.dto.PublisherResponse;
import dev.harakki.comics.catalog.dto.PublisherUpdateRequest;
import dev.harakki.comics.catalog.dto.ReplaceSlugRequest;
import dev.harakki.comics.shared.api.ApiProblemResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/publishers", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Publishers", description = "Management of publishing houses.")
@ApiProblemResponses
class PublisherController {

    private final PublisherService publisherService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create publisher", description = "Add a new publisher.")
    @ApiResponse(responseCode = "201", description = "Publisher created", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PublisherResponse.class)))
    public PublisherResponse createPublisher(@RequestBody @Valid PublisherCreateRequest request) {
        return publisherService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update publisher", description = "Update publisher details.")
    @ApiResponse(responseCode = "200", description = "Publisher updated")
    public PublisherResponse updatePublisher(
            @PathVariable @NotNull UUID id,
            @RequestBody @Valid PublisherUpdateRequest request
    ) {
        return publisherService.update(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get publisher by ID")
    @ApiResponse(responseCode = "200", description = "Publisher found")
    public PublisherResponse getPublisher(@PathVariable @NotNull UUID id) {
        return publisherService.getById(id);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get publisher by slug")
    @ApiResponse(responseCode = "200", description = "Publisher found")
    public PublisherResponse getPublisherBySlug(
            @Parameter(description = "URL slug", example = "shueisha")
            @PathVariable @NotNull String slug
    ) {
        return publisherService.getBySlug(slug);
    }

    @GetMapping
    @Operation(summary = "Search and filter publishers", description = "Retrieves publishers with optional filtering.")
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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete publisher", description = "Delete publisher entry.")
    @ApiResponse(responseCode = "204", description = "Publisher deleted")
    public void deletePublisher(@PathVariable @NotNull UUID id) {
        publisherService.delete(id);
    }

    @PutMapping("/{id}/slug")
    @Operation(summary = "Update slug")
    public PublisherResponse updatePublisherSlug(
            @PathVariable @NotNull UUID id,
            @RequestBody @Valid ReplaceSlugRequest request
    ) {
        return publisherService.updateSlug(id, request);
    }

}
