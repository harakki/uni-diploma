package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.TagService;
import dev.harakki.comics.catalog.dto.TagCreateRequest;
import dev.harakki.comics.catalog.dto.TagResponse;
import dev.harakki.comics.catalog.dto.TagUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/")
class TagController {

    private final TagService tagService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagResponse createTag(@RequestBody @Valid TagCreateRequest request) {
        return tagService.create(request);
    }

    @PutMapping("/{id}")
    public TagResponse updateTag(@PathVariable UUID id, @RequestBody @Valid TagUpdateRequest request) {
        return tagService.update(id, request);
    }

    @GetMapping("/{id}")
    public TagResponse getTag(@PathVariable UUID id) {
        return tagService.getById(id);
    }

    @GetMapping("/slug/{slug}")
    public TagResponse getTagBySlug(@PathVariable String slug) {
        return tagService.getBySlug(slug);
    }

    @GetMapping
    public Page<TagResponse> getAllTags(Pageable pageable) {
        return tagService.getAll(pageable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTag(@PathVariable UUID id) {
        tagService.delete(id);
    }

}
