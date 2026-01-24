package dev.harakki.comics.collections.web;

import dev.harakki.comics.collections.application.CollectionService;
import dev.harakki.comics.collections.dto.CollectionCreateRequest;
import dev.harakki.comics.collections.dto.CollectionUpdateRequest;
import dev.harakki.comics.collections.dto.UserCollectionResponse;
import dev.harakki.comics.shared.api.ApiProblemResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/collections", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Collections", description = "User collections management")
@ApiProblemResponses
@SecurityRequirement(name = "bearer-jwt")
public class UserCollectionController {

    private final CollectionService collectionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create collection", description = "Create a new user collection")
    @ApiResponse(responseCode = "201", description = "Collection created")
    public UserCollectionResponse create(@RequestBody @Valid CollectionCreateRequest request) {
        return collectionService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get collection by id", description = "Retrieve collection, respect privacy")
    public UserCollectionResponse getById(@PathVariable @NotNull UUID id) {
        return collectionService.getById(id);
    }

    @GetMapping
    @Operation(summary = "Search public collections", description = "Search public collections by name")
    public Page<UserCollectionResponse> search(@RequestParam(required = false) String search, @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return collectionService.search(search, pageable);
    }

    @GetMapping("/my")
    @Operation(summary = "Get my collections", description = "Get all collections of the current user")
    public Page<UserCollectionResponse> getMyCollections(@RequestParam(required = false) String search, @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return collectionService.getMyCollections(search, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update collection", description = "Update collection metadata and contained titles")
    @ApiResponse(responseCode = "200", description = "Collection updated")
    public UserCollectionResponse update(@PathVariable UUID id, @RequestBody @Valid CollectionUpdateRequest request) {
        return collectionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete collection", description = "Delete user's collection")
    public void delete(@PathVariable UUID id) {
        collectionService.delete(id);
    }

    @PostMapping("/{id}/share")
    @Operation(summary = "Generate share link", description = "Generate a unique share link for the collection")
    @ApiResponse(responseCode = "200", description = "Share link generated")
    public UserCollectionResponse generateShareLink(@PathVariable @NotNull UUID id) {
        return collectionService.generateShareToken(id);
    }

    @GetMapping("/shared/{shareToken}")
    @Operation(summary = "Get collection by share link", description = "Access collection via share token (no auth required)")
    @SecurityRequirement(name = "")
    public UserCollectionResponse getByShareToken(@PathVariable @NotNull String shareToken) {
        return collectionService.getByShareToken(shareToken);
    }

    @DeleteMapping("/{id}/share")
    @Operation(summary = "Revoke share link", description = "Revoke the share link, making collection inaccessible via previous link")
    @ApiResponse(responseCode = "200", description = "Share link revoked")
    public UserCollectionResponse revokeShareLink(@PathVariable @NotNull UUID id) {
        return collectionService.revokeShareToken(id);
    }

    @PostMapping("/{id}/titles")
    @Operation(summary = "Add titles to collection", description = "Add titles (by id list) to user's collection in order")
    public UserCollectionResponse addTitles(@PathVariable UUID id, @RequestBody List<UUID> titleIds) {
        return collectionService.addTitles(id, titleIds);
    }

    @PostMapping("/{id}/titles")
    @Operation(summary = "Add title to collection", description = "Add a single title to user's collection at the end")
    public UserCollectionResponse addTitle(@PathVariable UUID id, @RequestBody UUID titleId) {
        return collectionService.addTitle(id, titleId);
    }

    @DeleteMapping("/{id}/titles/{titleId}")
    @Operation(summary = "Remove title from collection", description = "Remove a single title from collection")
    public UserCollectionResponse removeTitle(@PathVariable UUID id, @PathVariable UUID titleId) {
        return collectionService.removeTitle(id, titleId);
    }

}
