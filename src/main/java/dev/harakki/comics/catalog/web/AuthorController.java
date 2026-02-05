package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.AuthorService;
import dev.harakki.comics.catalog.domain.Author;
import dev.harakki.comics.catalog.dto.AuthorCreateRequest;
import dev.harakki.comics.catalog.dto.AuthorResponse;
import dev.harakki.comics.catalog.dto.AuthorUpdateRequest;
import dev.harakki.comics.catalog.dto.ReplaceSlugRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.LikeIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
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
@RequestMapping(path = "/api/v1/authors", produces = MediaType.APPLICATION_JSON_VALUE)
class AuthorController implements AuthorApi {

    private final AuthorService authorService;

    @GetMapping("/{id}")
    public AuthorResponse getAuthor(@PathVariable @NotNull UUID id) {
        return authorService.getById(id);
    }

    @GetMapping("/slug/{slug}")
    public AuthorResponse getAuthorBySlug(@PathVariable @NotNull String slug) {
        return authorService.getBySlug(slug);
    }

    @GetMapping
    public Page<AuthorResponse> getAllAuthors(
            @Or({
                    @Spec(path = "name", params = "search", spec = LikeIgnoreCase.class),
                    @Spec(path = "slug", params = "search", spec = LikeIgnoreCase.class)
            }) Specification<Author> searchSpec,
            @And({
                    @Spec(path = "countryIsoCode", params = "country", spec = Equal.class)
            }) Specification<Author> filterSpec,
            @PageableDefault(sort = "name") Pageable pageable
    ) {
        Specification<Author> spec = Specification.where(searchSpec).and(filterSpec);
        return authorService.getAll(spec, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorResponse createAuthor(@RequestBody @Valid AuthorCreateRequest request) {
        return authorService.create(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public AuthorResponse updateAuthor(@PathVariable UUID id, @RequestBody @Valid AuthorUpdateRequest request) {
        return authorService.update(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}" + "/slug")
    public AuthorResponse updateAuthorSlug(@PathVariable @NotNull UUID id,
                                           @RequestBody @Valid ReplaceSlugRequest request) {
        return authorService.updateSlug(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuthor(@PathVariable @NotNull UUID id) {
        authorService.delete(id);
    }

}
