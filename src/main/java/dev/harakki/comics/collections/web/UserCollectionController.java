package dev.harakki.comics.collections.web;

import dev.harakki.comics.collections.application.CollectionService;
import dev.harakki.comics.collections.dto.CollectionCreateRequest;
import dev.harakki.comics.collections.dto.CollectionUpdateRequest;
import dev.harakki.comics.collections.dto.UserCollectionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = UserCollectionController.REQUEST_MAPPING, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Collections", description = "User collections management")
@SecurityRequirement(name = "bearer-jwt")
public class UserCollectionController {

    static final String REQUEST_MAPPING = "/api/v1/collections";

    static final String BY_ID = "/{id}";

    private final CollectionService collectionService;

    @Operation(
            operationId = "createCollection",
            summary = "Create collection",
            description = "Create a new user collection"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Collection created",
                    content = @Content(schema = @Schema(implementation = UserCollectionResponse.class))),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "400", ref = "BadRequest")
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserCollectionResponse create(@RequestBody @Valid CollectionCreateRequest request) {
        return collectionService.create(request);
    }

    @Operation(
            operationId = "getCollectionById",
            summary = "Get collection by id",
            description = "Retrieve collection, respect privacy"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Collection retrieved",
                    content = @Content(schema = @Schema(implementation = UserCollectionResponse.class))),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @GetMapping(BY_ID)
    public UserCollectionResponse getById(
            @Parameter(description = "Collection UUID", required = true)
            @PathVariable @NotNull UUID id
    ) {
        return collectionService.getById(id);
    }

    @Operation(
            operationId = "searchPublicCollections",
            summary = "Search public collections",
            description = "Search public collections by name"
    )
    @GetMapping
    public Page<UserCollectionResponse> search(
            @Parameter(description = "Search query")
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {
        return collectionService.search(search, pageable);
    }

    @Operation(
            operationId = "getMyCollections",
            summary = "Get my collections",
            description = "Get all collections of the current user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Collections retrieved",
                    content = @Content(schema = @Schema(implementation = UserCollectionResponse.class))),
            @ApiResponse(responseCode = "401", ref = "Unauthorized")
    })
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public Page<UserCollectionResponse> getMyCollections(
            @Parameter(description = "Search query")
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {
        return collectionService.getMyCollections(search, pageable);
    }

    @Operation(
            operationId = "updateCollection",
            summary = "Update collection",
            description = "Update collection metadata and contained titles"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Collection updated",
                    content = @Content(schema = @Schema(implementation = UserCollectionResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @PreAuthorize("hasRole('USER')")
    @PutMapping(BY_ID)
    public UserCollectionResponse update(
            @Parameter(description = "Collection UUID", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid CollectionUpdateRequest request
    ) {
        return collectionService.update(id, request);
    }

    @Operation(
            operationId = "deleteCollection",
            summary = "Delete collection",
            description = "Delete user's collection"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Collection deleted"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping(BY_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "Collection UUID", required = true)
            @PathVariable UUID id
    ) {
        collectionService.delete(id);
    }

    @Operation(
            operationId = "generateShareLink",
            summary = "Generate share link",
            description = "Generate a unique share link for the collection"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Share link generated",
                    content = @Content(schema = @Schema(implementation = UserCollectionResponse.class))),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping(BY_ID + "/share")
    public UserCollectionResponse generateShareLink(
            @Parameter(description = "Collection UUID", required = true)
            @PathVariable @NotNull UUID id
    ) {
        return collectionService.generateShareToken(id);
    }

    @Operation(
            operationId = "getCollectionByShareToken",
            summary = "Get collection by share link",
            description = "Access collection via share token (no auth required)",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Collection retrieved",
                    content = @Content(schema = @Schema(implementation = UserCollectionResponse.class))),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @GetMapping("/shared/{shareToken}")
    public UserCollectionResponse getByShareToken(
            @Parameter(description = "Share token", required = true)
            @PathVariable @NotNull String shareToken
    ) {
        return collectionService.getByShareToken(shareToken);
    }

    @Operation(
            operationId = "revokeShareLink",
            summary = "Revoke share link",
            description = "Revoke the share link, making collection inaccessible via previous link"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Share link revoked",
                    content = @Content(schema = @Schema(implementation = UserCollectionResponse.class))),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping(BY_ID + "/share")
    public UserCollectionResponse revokeShareLink(
            @Parameter(description = "Collection UUID", required = true)
            @PathVariable @NotNull UUID id
    ) {
        return collectionService.revokeShareToken(id);
    }

    @Operation(
            operationId = "addTitlesToCollection",
            summary = "Add titles to collection",
            description = "Add titles (by id list) to user's collection in order"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Titles added",
                    content = @Content(schema = @Schema(implementation = UserCollectionResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @PreAuthorize("hasRole('USER')")
    @PostMapping(BY_ID + "/titles")
    public UserCollectionResponse addTitles(
            @Parameter(description = "Collection UUID", required = true)
            @PathVariable UUID id,
            @RequestBody List<UUID> titleIds
    ) {
        return collectionService.addTitles(id, titleIds);
    }

    @Operation(
            operationId = "removeTitleFromCollection",
            summary = "Remove title from collection",
            description = "Remove a single title from collection"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Title removed",
                    content = @Content(schema = @Schema(implementation = UserCollectionResponse.class))),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping(BY_ID + "/titles/{titleId}")
    public UserCollectionResponse removeTitle(
            @Parameter(description = "Collection UUID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Title UUID", required = true)
            @PathVariable UUID titleId
    ) {
        return collectionService.removeTitle(id, titleId);
    }

}
