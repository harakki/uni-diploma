package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.PublisherService;
import dev.harakki.comics.catalog.dto.PublisherCreateRequest;
import dev.harakki.comics.catalog.dto.PublisherResponse;
import dev.harakki.comics.catalog.dto.PublisherUpdateRequest;
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
@RequestMapping("/api/v1/publishers")
class PublisherController {

    private final PublisherService publisherService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PublisherResponse createPublisher(@RequestBody @Valid PublisherCreateRequest request) {
        return publisherService.create(request);
    }

    @PutMapping("/{id}")
    public PublisherResponse updatePublisher(@PathVariable @NotNull UUID id, @RequestBody @Valid PublisherUpdateRequest request) {
        return publisherService.update(id, request);
    }

    @GetMapping("/{id}")
    public PublisherResponse getPublisher(@PathVariable @NotNull UUID id) {
        return publisherService.getById(id);
    }
    
    @GetMapping("/slug/{slug}")
    public PublisherResponse getPublisherBySlug(@PathVariable @NotNull String slug) {
        return publisherService.getBySlug(slug);
    }

    @GetMapping
    public Page<PublisherResponse> getAllPublishers(@PageableDefault(sort = "name") Pageable pageable) {
        return publisherService.getAll(pageable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePublisher(@PathVariable @NotNull UUID id) {
        publisherService.delete(id);
    }

    @PutMapping("/{id}/slug")
    public PublisherResponse updatePublisherSlug(@PathVariable @NotNull UUID id, @RequestBody @NotBlank String slug) {
        return publisherService.updateSlug(id, slug);
    }

}
