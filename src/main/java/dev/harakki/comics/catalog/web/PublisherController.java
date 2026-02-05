package dev.harakki.comics.catalog.web;

import dev.harakki.comics.catalog.application.PublisherService;
import dev.harakki.comics.catalog.domain.Publisher;
import dev.harakki.comics.catalog.dto.PublisherCreateRequest;
import dev.harakki.comics.catalog.dto.PublisherResponse;
import dev.harakki.comics.catalog.dto.PublisherUpdateRequest;
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
@RequestMapping(path = "/api/v1/publishers", produces = MediaType.APPLICATION_JSON_VALUE)
class PublisherController implements PublisherApi {

    private final PublisherService publisherService;

    @GetMapping("/{id}")
    public PublisherResponse getPublisher(@PathVariable @NotNull UUID id) {
        return publisherService.getById(id);
    }

    @GetMapping("/slug/{slug}")
    public PublisherResponse getPublisherBySlug(@PathVariable @NotNull String slug) {
        return publisherService.getBySlug(slug);
    }

    @GetMapping
    public Page<PublisherResponse> getAllPublishers(
            @Or({
                    @Spec(path = "name", params = "search", spec = LikeIgnoreCase.class),
                    @Spec(path = "slug", params = "search", spec = LikeIgnoreCase.class)
            }) Specification<Publisher> searchSpec,
            @And({
                    @Spec(path = "countryIsoCode", params = "country", spec = Equal.class)
            }) Specification<Publisher> filterSpec,
            @PageableDefault(sort = "name") Pageable pageable
    ) {
        Specification<Publisher> spec = Specification.where(searchSpec).and(filterSpec);
        return publisherService.getAll(spec, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PublisherResponse createPublisher(@RequestBody @Valid PublisherCreateRequest request) {
        return publisherService.create(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public PublisherResponse updatePublisher(@PathVariable @NotNull UUID id,
                                             @RequestBody @Valid PublisherUpdateRequest request) {
        return publisherService.update(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}" + "/slug")
    public PublisherResponse updatePublisherSlug(
            @PathVariable @NotNull UUID id,
            @RequestBody @Valid ReplaceSlugRequest request
    ) {
        return publisherService.updateSlug(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePublisher(@PathVariable @NotNull UUID id) {
        publisherService.delete(id);
    }

}
