package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.AuthorService;
import dev.harakki.comics.catalog.dto.AuthorCreateRequest;
import dev.harakki.comics.catalog.dto.AuthorResponse;
import dev.harakki.comics.catalog.dto.AuthorUpdateRequest;
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
@RequestMapping(path = "/api/v1/authors", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authors", description = "Management of comic creators.")
@ApiProblemResponses
class AuthorController {

    private final AuthorService authorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create author", description = "Add a new author.")
    @ApiResponse(responseCode = "201", description = "Author created successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthorResponse.class)))
    public AuthorResponse createAuthor(@RequestBody @Valid AuthorCreateRequest request) {
        return authorService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update author", description = "Update author personal details.")
    @ApiResponse(responseCode = "200", description = "Author updated")
    public AuthorResponse updateAuthor(
            @PathVariable UUID id,
            @RequestBody @Valid AuthorUpdateRequest request
    ) {
        return authorService.update(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get author by ID", description = "Retrieve author details by UUID.")
    @ApiResponse(responseCode = "200", description = "Author found")
    public AuthorResponse getAuthor(@PathVariable @NotNull UUID id) {
        return authorService.getById(id);
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get author by slug", description = "SEO-friendly retrieval.")
    @ApiResponse(responseCode = "200", description = "Author found")
    public AuthorResponse getAuthorBySlug(
            @Parameter(description = "URL slug", example = "tatsuki-fujimoto")
            @PathVariable @NotNull String slug
    ) {
        return authorService.getBySlug(slug);
    }

    @GetMapping
    @Operation(summary = "Get all authors", description = "Paginated list of authors.")
    public Page<AuthorResponse> getAllAuthors(
            @ParameterObject @PageableDefault(sort = "name") Pageable pageable
    ) {
        return authorService.getAll(pageable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete author", description = "Delete author entry.")
    @ApiResponse(responseCode = "204", description = "Author deleted")
    public void deleteAuthor(@PathVariable @NotNull UUID id) {
        authorService.delete(id);
    }

    @PutMapping("/{id}/slug")
    @Operation(summary = "Update slug", description = "Manually change the URL slug.")
    public AuthorResponse updateAuthorSlug(
            @PathVariable @NotNull UUID id,
            @RequestBody @NotBlank @Parameter(description = "New slug", example = "fujimoto") String slug
    ) {
        return authorService.updateSlug(id, slug);
    }

}
