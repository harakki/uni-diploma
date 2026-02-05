package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.TagService;
import dev.harakki.comics.catalog.dto.ReplaceSlugRequest;
import dev.harakki.comics.catalog.dto.TagCreateRequest;
import dev.harakki.comics.catalog.dto.TagResponse;
import dev.harakki.comics.catalog.dto.TagUpdateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/tags", produces = MediaType.APPLICATION_JSON_VALUE)
class TagController implements TagApi {

    private final TagService tagService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TagResponse createTag(@RequestBody @Valid TagCreateRequest request) {
        return tagService.create(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public TagResponse updateTag(@PathVariable @NotNull UUID id, @RequestBody @Valid TagUpdateRequest request) {
        return tagService.update(id, request);
    }

    @GetMapping("/{id}")
    public TagResponse getTag(@PathVariable @NotNull UUID id) {
        return tagService.getById(id);
    }

    @GetMapping("/slug/{slug}")
    public TagResponse getTagBySlug(@PathVariable String slug) {
        return tagService.getBySlug(slug);
    }

    @GetMapping
    public Page<TagResponse> getAllTags(@PageableDefault(sort = "name") Pageable pageable) {
        return tagService.getAll(pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTag(@PathVariable @NotNull UUID id) {
        tagService.delete(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}" + "/slug")
    public TagResponse updateTagSlug(@PathVariable @NotNull UUID id, @RequestBody @Valid ReplaceSlugRequest request) {
        return tagService.updateSlug(id, request);
    }

}
