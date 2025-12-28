package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.TitleService;
import dev.harakki.comics.catalog.dto.TitleCreateRequest;
import dev.harakki.comics.catalog.dto.TitleResponse;
import dev.harakki.comics.catalog.dto.TitleUpdateRequest;
import dev.harakki.comics.catalog.dto.UpdateTitleAddAuthorRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/titles")
class TitleController {

    private final TitleService titleService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TitleResponse createTitle(@RequestBody @Valid TitleCreateRequest request) {
        return titleService.create(request);
    }

    @PutMapping("/{id}")
    public TitleResponse updateTitle(@PathVariable @NotNull UUID id, @RequestBody @Valid TitleUpdateRequest request) {
        return titleService.update(id, request);
    }

    @GetMapping("/{id}")
    public TitleResponse getTitle(@PathVariable @NotNull UUID id) {
        return titleService.getById(id);
    }

    @GetMapping("/slug/{slug}")
    public TitleResponse getTitleBySlug(@PathVariable @NotNull String slug) {
        return titleService.getBySlug(slug);
    }

    @GetMapping
    public Page<TitleResponse> getAllTitles(@PageableDefault(sort = "name") Pageable pageable) {
        return titleService.getAll(pageable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTitle(@PathVariable @NotNull UUID id) {
        titleService.delete(id);
    }

    @PutMapping("/{id}/slug")
    public TitleResponse updateTitleSlug(@PathVariable @NotNull UUID id, @RequestBody @NotBlank String slug) {
        return titleService.updateSlug(id, slug);
    }

    @PostMapping("/{id}/authors")
    public void addAuthor(@PathVariable @NotNull UUID id, @RequestBody @Valid UpdateTitleAddAuthorRequest request) {
        titleService.addAuthor(id, request.authorId(), request.role());
    }

    @DeleteMapping("/{id}/authors/{authorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAuthor(@PathVariable @NotNull UUID id, @PathVariable @NotNull UUID authorId) {
        titleService.removeAuthor(id, authorId);
    }

    @DeleteMapping("/{id}/publisher")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePublisher(@PathVariable @NotNull UUID id) {
        titleService.removePublisher(id);
    }

    @PostMapping("/{id}/tags")
    public void updateTags(@PathVariable @NotNull UUID id, @RequestBody @NotEmpty Set<UUID> tagIds) {
        titleService.updateTags(id, tagIds);
    }

}
