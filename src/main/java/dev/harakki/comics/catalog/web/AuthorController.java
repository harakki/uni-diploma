package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.AuthorService;
import dev.harakki.comics.catalog.domain.Author;
import dev.harakki.comics.catalog.dto.AuthorCreateRequest;
import dev.harakki.comics.catalog.dto.AuthorResponse;
import dev.harakki.comics.catalog.dto.AuthorUpdateRequest;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/authors", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authors", description = "Management of comic creators.")
class AuthorController {

    private final AuthorService authorService;

    @Operation(
            operationId = "createAuthor",
            summary = "Create author",
            description = "Add a new author."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Author created successfully",
                    content = @Content(schema = @Schema(implementation = AuthorResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "409", ref = "Conflict")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorResponse createAuthor(@RequestBody @Valid AuthorCreateRequest request) {
        return authorService.create(request);
    }

    @Operation(
            operationId = "updateAuthor",
            summary = "Update author",
            description = "Update author personal details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Author updated",
                    content = @Content(schema = @Schema(implementation = AuthorResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @PutMapping("/{id}")
    public AuthorResponse updateAuthor(
            @Parameter(description = "Author UUID", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid AuthorUpdateRequest request
    ) {
        return authorService.update(id, request);
    }

    @Operation(
            operationId = "getAuthorById",
            summary = "Get author by ID",
            description = "Retrieve author details by UUID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Author found",
                    content = @Content(schema = @Schema(implementation = AuthorResponse.class))),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @GetMapping("/{id}")
    public AuthorResponse getAuthor(
            @Parameter(description = "Author UUID", required = true)
            @PathVariable @NotNull UUID id
    ) {
        return authorService.getById(id);
    }

    @Operation(
            operationId = "getAuthorBySlug",
            summary = "Get author by slug",
            description = "SEO-friendly retrieval."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Author found",
                    content = @Content(schema = @Schema(implementation = AuthorResponse.class))),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @GetMapping("/slug/{slug}")
    public AuthorResponse getAuthorBySlug(
            @Parameter(description = "URL slug", example = "tatsuki-fujimoto", required = true)
            @PathVariable @NotNull String slug
    ) {
        return authorService.getBySlug(slug);
    }

    @Operation(
            operationId = "searchAuthors",
            summary = "Search and filter authors",
            description = "Retrieves authors with optional filtering."
    )
    @Parameters({
            @Parameter(name = "search", description = "Search by name or slug", example = "fujimoto"),
            @Parameter(name = "country", description = "Filter by Country ISO Code", example = "JP")
    })
    @GetMapping
    public Page<AuthorResponse> getAllAuthors(
            @Or({
                    @Spec(path = "name", params = "search", spec = LikeIgnoreCase.class),
                    @Spec(path = "slug", params = "search", spec = LikeIgnoreCase.class)
            }) @Parameter(hidden = true) Specification<Author> searchSpec,
            @And({
                    @Spec(path = "countryIsoCode", params = "country", spec = Equal.class)
            }) @Parameter(hidden = true) Specification<Author> filterSpec,
            @ParameterObject @PageableDefault(sort = "name") Pageable pageable
    ) {
        Specification<Author> spec = Specification.where(searchSpec).and(filterSpec);
        return authorService.getAll(spec, pageable);
    }

    @Operation(
            operationId = "deleteAuthor",
            summary = "Delete author",
            description = "Delete author entry."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Author deleted"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuthor(
            @Parameter(description = "Author UUID", required = true)
            @PathVariable @NotNull UUID id
    ) {
        authorService.delete(id);
    }

    @Operation(
            operationId = "updateAuthorSlug",
            summary = "Update slug",
            description = "Manually change the URL slug."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slug updated",
                    content = @Content(schema = @Schema(implementation = AuthorResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "409", ref = "Conflict")
    })
    @PutMapping("/{id}/slug")
    public AuthorResponse updateAuthorSlug(
            @Parameter(description = "Author UUID", required = true)
            @PathVariable @NotNull UUID id,
            @RequestBody @Valid ReplaceSlugRequest request
    ) {
        return authorService.updateSlug(id, request);
    }

}
