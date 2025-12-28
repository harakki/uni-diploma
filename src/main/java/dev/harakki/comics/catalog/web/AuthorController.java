package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.AuthorService;
import dev.harakki.comics.catalog.dto.AuthorResponse;
import dev.harakki.comics.catalog.dto.AuthorCreateRequest;
import dev.harakki.comics.catalog.dto.AuthorUpdateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/authors")
class AuthorController {

    private final AuthorService authorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorResponse createAuthor(@RequestBody @Valid AuthorCreateRequest request) {
        return authorService.create(request);
    }

    @PutMapping("/{id}")
    public AuthorResponse updateAuthor(@PathVariable UUID id, @RequestBody @Valid AuthorUpdateRequest request) {
        return authorService.update(id, request);
    }

    @GetMapping("/{id}")
    public AuthorResponse getAuthor(@PathVariable @NotNull UUID id) {
        return authorService.getById(id);
    }

    @GetMapping("/slug/{slug}")
    public AuthorResponse getAuthorBySlug(@PathVariable @NotNull String slug) {
        return authorService.getBySlug(slug);
    }

    @GetMapping
    public Page<AuthorResponse> getAllAuthors(@PageableDefault(sort = "name") Pageable pageable) {
        return authorService.getAll(pageable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuthor(@PathVariable @NotNull UUID id) {
        authorService.delete(id);
    }

    @PutMapping("/{id}/slug")
    public AuthorResponse updateAuthorSlug(@PathVariable @NotNull UUID id, @RequestBody @NotBlank String slug) {
        return authorService.updateSlug(id, slug);
    }

}
