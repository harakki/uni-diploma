package dev.harakki.comics.library.web;

import dev.harakki.comics.library.application.LibraryService;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springdoc.core.annotations.ParameterObject;
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
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('USER')")
@RestController
@RequestMapping(path = LibraryController.REQUEST_MAPPING, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Library", description = "User's personal library management")
class LibraryController {

    static final String REQUEST_MAPPING = "/api/v1/library";

    private final LibraryService libraryService;

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
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LibraryEntryResponse addToLibrary(@RequestBody @Valid LibraryEntryCreateRequest request) {
        return libraryService.addToLibrary(request);
    }

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
    @GetMapping("/{entryId}")
    public LibraryEntryResponse getEntry(
            @Parameter(description = "Library entry UUID", required = true)
            @PathVariable @NotNull UUID entryId
    ) {
        return libraryService.getById(entryId);
    }

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
    @GetMapping
    public Page<LibraryEntryResponse> getMyLibrary(
            @And({
                    @Spec(path = "status", params = "status", spec = Equal.class),
                    @Spec(path = "title.id", params = "titleId", spec = Equal.class)
            }) @Parameter(hidden = true) Specification<LibraryEntry> spec,
            @ParameterObject @PageableDefault(sort = "updatedAt") Pageable pageable
    ) {
        return libraryService.searchLibrary(spec, pageable);
    }

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
    @PutMapping("/{entryId}")
    public LibraryEntryResponse updateEntry(
            @Parameter(description = "Library entry UUID", required = true)
            @PathVariable UUID entryId,
            @RequestBody @Valid LibraryEntryUpdateRequest request
    ) {
        return libraryService.update(entryId, request);
    }

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
    @DeleteMapping("/{entryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFromLibrary(
            @Parameter(description = "Library entry UUID", required = true)
            @PathVariable UUID entryId
    ) {
        libraryService.removeFromLibrary(entryId);
    }

}
