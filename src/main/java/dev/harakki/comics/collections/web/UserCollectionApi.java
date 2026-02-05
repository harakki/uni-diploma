package dev.harakki.comics.collections.web;

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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Tag(name = "Collections", description = "User collections management")
@SecurityRequirement(name = "bearer-jwt")
public interface UserCollectionApi {

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
    UserCollectionResponse create(CollectionCreateRequest request);

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
    UserCollectionResponse getById(
            @Parameter(description = "Collection UUID", required = true) UUID id
    );

    @Operation(
            operationId = "searchPublicCollections",
            summary = "Search public collections",
            description = "Search public collections by name"
    )
    Page<UserCollectionResponse> search(
            @Parameter(description = "Search query") String search,
            @ParameterObject Pageable pageable
    );

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
    Page<UserCollectionResponse> getMyCollections(
            @Parameter(description = "Search query") String search,
            @ParameterObject Pageable pageable
    );

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
    UserCollectionResponse update(
            @Parameter(description = "Collection UUID", required = true) UUID id,
            CollectionUpdateRequest request
    );

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
    void delete(
            @Parameter(description = "Collection UUID", required = true) UUID id
    );

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
    UserCollectionResponse generateShareLink(
            @Parameter(description = "Collection UUID", required = true) UUID id
    );

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
    UserCollectionResponse getByShareToken(
            @Parameter(description = "Share token", required = true) String shareToken
    );

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
    UserCollectionResponse revokeShareLink(
            @Parameter(description = "Collection UUID", required = true) UUID id
    );

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
    UserCollectionResponse addTitles(
            @Parameter(description = "Collection UUID", required = true) UUID id,
            List<UUID> titleIds
    );

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
    UserCollectionResponse removeTitle(
            @Parameter(description = "Collection UUID", required = true) UUID id,
            @Parameter(description = "Title UUID", required = true) UUID titleId
    );

}
