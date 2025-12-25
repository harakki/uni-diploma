package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.PublisherService;
import dev.harakki.comics.catalog.dto.PublisherCreateRequest;
import dev.harakki.comics.catalog.dto.PublisherResponse;
import dev.harakki.comics.catalog.dto.PublisherUpdateRequest;
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
class PublisherController {

    private final PublisherService publisherService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PublisherResponse createPublisher(@RequestBody @Valid PublisherCreateRequest request) {
        return publisherService.create(request);
    }

    @PutMapping("/{id}")
    public PublisherResponse updatePublisher(@PathVariable UUID id, @RequestBody @Valid PublisherUpdateRequest request) {
        return publisherService.update(id, request);
    }

    @GetMapping("/{id}")
    public PublisherResponse getPublisher(@PathVariable UUID id) {
        return publisherService.getById(id);
    }
    
    @GetMapping("/slug/{slug}")
    public PublisherResponse getPublisherBySlug(@PathVariable String slug) {
        return publisherService.getBySlug(slug);
    }

    @GetMapping
    public Page<PublisherResponse> getAllPublishers(Pageable pageable) {
        return publisherService.getAll(pageable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePublisher(@PathVariable UUID id) {
        publisherService.delete(id);
    }

}
