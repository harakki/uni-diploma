package dev.harakki.comics.collections.web;

import dev.harakki.comics.collections.application.CollectionService;
import dev.harakki.comics.collections.dto.CollectionCreateRequest;
import dev.harakki.comics.collections.dto.CollectionUpdateRequest;
import dev.harakki.comics.collections.dto.UserCollectionResponse;
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

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/collections", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserCollectionController implements UserCollectionApi {

    private final CollectionService collectionService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserCollectionResponse create(@RequestBody @Valid CollectionCreateRequest request) {
        return collectionService.create(request);
    }

    @GetMapping("/{id}")
    public UserCollectionResponse getById(@PathVariable @NotNull UUID id) {
        return collectionService.getById(id);
    }

    @GetMapping
    public Page<UserCollectionResponse> search(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return collectionService.search(search, pageable);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public Page<UserCollectionResponse> getMyCollections(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return collectionService.getMyCollections(search, pageable);
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{id}")
    public UserCollectionResponse update(
            @PathVariable UUID id,
            @RequestBody @Valid CollectionUpdateRequest request
    ) {
        return collectionService.update(id, request);
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        collectionService.delete(id);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{id}" + "/share")
    public UserCollectionResponse generateShareLink(@PathVariable @NotNull UUID id) {
        return collectionService.generateShareToken(id);
    }

    @GetMapping("/shared/{shareToken}")
    public UserCollectionResponse getByShareToken(@PathVariable @NotNull String shareToken) {
        return collectionService.getByShareToken(shareToken);
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}" + "/share")
    public UserCollectionResponse revokeShareLink(@PathVariable @NotNull UUID id) {
        return collectionService.revokeShareToken(id);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{id}" + "/titles")
    public UserCollectionResponse addTitles(
            @PathVariable UUID id,
            @RequestBody List<UUID> titleIds
    ) {
        return collectionService.addTitles(id, titleIds);
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}" + "/titles/{titleId}")
    public UserCollectionResponse removeTitle(
            @PathVariable UUID id,
            @PathVariable UUID titleId
    ) {
        return collectionService.removeTitle(id, titleId);
    }

}
