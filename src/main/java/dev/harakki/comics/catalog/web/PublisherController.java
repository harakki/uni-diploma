package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.PublisherService;
import dev.harakki.comics.catalog.dto.PublisherCreateRequest;
import dev.harakki.comics.catalog.dto.PublisherResponse;
import dev.harakki.comics.catalog.dto.PublisherUpdateRequest;
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
    @Operation(summary = "Get all publishers", description = "Paginated list of publishers.")
    public Page<PublisherResponse> getAllPublishers(
            @ParameterObject @PageableDefault(sort = "name") Pageable pageable
    ) {
        return publisherService.getAll(pageable);
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
            @RequestBody @NotBlank @Parameter(description = "New slug", example = "manga-plus") String slug
    ) {
        return publisherService.updateSlug(id, slug);
    }

}
