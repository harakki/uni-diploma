package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.TitleService;
import dev.harakki.comics.catalog.dto.TitleCreateRequest;
import dev.harakki.comics.catalog.dto.TitleResponse;
import dev.harakki.comics.catalog.dto.TitleUpdateRequest;
import dev.harakki.comics.catalog.dto.UpdateTitleAddAuthorRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public TitleResponse updateTitle(@PathVariable UUID id, @RequestBody @Valid TitleUpdateRequest request) {
        return titleService.update(id, request);
    }

    @GetMapping("/{id}")
    public TitleResponse getTitle(@PathVariable UUID id) {
        return titleService.getById(id);
    }

    @GetMapping("/slug/{slug}")
    public TitleResponse getTitleBySlug(@PathVariable String slug) {
        return titleService.getBySlug(slug);
    }

    @GetMapping
    public Page<TitleResponse> getAllTitles(Pageable pageable) {
        return titleService.getAll(pageable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTitle(@PathVariable UUID id) {
        titleService.delete(id);
    }

    @PostMapping("/{id}/authors")
    public void addAuthor(@PathVariable UUID id, @RequestBody @Valid UpdateTitleAddAuthorRequest request) {
        titleService.addAuthor(id, request.authorId(), request.role());
    }

    @DeleteMapping("/{id}/authors/{authorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAuthor(@PathVariable UUID id, @PathVariable UUID authorId) {
        titleService.removeAuthor(id, authorId);
    }

    @PostMapping("/{id}/tags")
    public void updateTags(@PathVariable UUID id, @RequestBody Set<UUID> tagIds) {
        titleService.updateTags(id, tagIds);
    }

}
