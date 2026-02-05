package dev.harakki.comics.library.web;

import dev.harakki.comics.library.domain.LibraryEntry;
import dev.harakki.comics.library.dto.LibraryEntryCreateRequest;
import dev.harakki.comics.library.dto.LibraryEntryResponse;
import dev.harakki.comics.library.dto.LibraryEntryUpdateRequest;
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
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

@Tag(name = "Library", description = "User's personal library management")
@SecurityRequirement(name = "bearer-jwt")
public interface LibraryApi {

    @Operation(
            operationId = "addToLibrary",
            summary = "Add title to library",
            description = "Add title to the authenticated user's library."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Title added to library",
                    content = @Content(schema = @Schema(implementation = LibraryEntryResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "409", ref = "Conflict")
    })
    LibraryEntryResponse addToLibrary(LibraryEntryCreateRequest request);

    @Operation(
            operationId = "getLibraryEntryById",
            summary = "Get library entry by ID",
            description = "Retrieve a specific library entry."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Library entry found",
                    content = @Content(schema = @Schema(implementation = LibraryEntryResponse.class))),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    LibraryEntryResponse getEntry(@Parameter(description = "Library entry UUID", required = true) UUID entryId);

    @Operation(
            operationId = "getMyLibrary",
            summary = "Get my library",
            description = "Retrieve the authenticated user's library with optional filters."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Library entries retrieved",
                    content = @Content(schema = @Schema(implementation = LibraryEntryResponse.class))),
            @ApiResponse(responseCode = "401", ref = "Unauthorized")
    })
    Page<LibraryEntryResponse> getMyLibrary(
            @Parameter(hidden = true) Specification<LibraryEntry> spec,
            @ParameterObject Pageable pageable
    );

    @Operation(
            operationId = "updateLibraryEntry",
            summary = "Update library entry",
            description = "Update reading status, rating, or progress of a title."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Library entry updated",
                    content = @Content(schema = @Schema(implementation = LibraryEntryResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    LibraryEntryResponse updateEntry(
            @Parameter(description = "Library entry UUID", required = true) UUID entryId,
            LibraryEntryUpdateRequest request
    );

    @Operation(
            operationId = "removeFromLibrary",
            summary = "Remove from library",
            description = "Remove a title from the user's library."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Title removed from library"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    void removeFromLibrary(@Parameter(description = "Library entry UUID", required = true) UUID entryId);

}
