package dev.harakki.comics.library.web;

import dev.harakki.comics.library.application.LibraryEntryService;
import dev.harakki.comics.library.domain.LibraryEntry;
import dev.harakki.comics.library.domain.ReadingStatus;
import dev.harakki.comics.library.dto.LibraryEntryCreateRequest;
import dev.harakki.comics.library.dto.LibraryEntryResponse;
import dev.harakki.comics.library.dto.LibraryEntryUpdateRequest;
import dev.harakki.comics.shared.api.ApiProblemResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/library", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Library", description = "User's personal library management")
@ApiProblemResponses
@SecurityRequirement(name = "bearer-jwt")
class LibraryEntryController {

    private final LibraryEntryService libraryEntryService;

    @PostMapping("/entries")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add title to library", description = "Add title to the authenticated user's library.")
    @ApiResponse(responseCode = "201", description = "Title added to library",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LibraryEntryResponse.class)))
    public LibraryEntryResponse addToLibrary(@RequestBody @Valid LibraryEntryCreateRequest request) {
        return libraryEntryService.addToLibrary(request);
    }

    @PutMapping("/entries/{id}")
    @Operation(summary = "Update library entry", description = "Update reading status, rating, or progress of a title.")
    @ApiResponse(responseCode = "200", description = "Library entry updated")
    public LibraryEntryResponse updateEntry(
            @PathVariable UUID id,
            @RequestBody @Valid LibraryEntryUpdateRequest request
    ) {
        return libraryEntryService.update(id, request);
    }

    @DeleteMapping("/entries/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove from library", description = "Remove a title from the user's library.")
    @ApiResponse(responseCode = "204", description = "Title removed from library")
    public void removeFromLibrary(@PathVariable UUID id) {
        libraryEntryService.removeFromLibrary(id);
    }

    @GetMapping("/entries/{id}")
    @Operation(summary = "Get library entry by ID", description = "Retrieve a specific library entry.")
    @ApiResponse(responseCode = "200", description = "Library entry found")
    public LibraryEntryResponse getEntry(@PathVariable @NotNull UUID id) {
        return libraryEntryService.getById(id);
    }

    @GetMapping("/entries/by-title/{titleId}")
    @Operation(summary = "Get library entry by title", description = "Check if a title is in the user's library.")
    @ApiResponse(responseCode = "200", description = "Library entry found")
    public LibraryEntryResponse getByTitleId(@PathVariable @NotNull UUID titleId) {
        return libraryEntryService.getByTitleId(titleId);
    }

    @GetMapping("/entries")
    @Operation(summary = "Get my library", description = "Retrieve the authenticated user's library with optional filters.")
    @Parameters({
            @Parameter(name = "status", description = "Filter by reading status", example = "READING")
    })
    public Page<LibraryEntryResponse> getMyLibrary(
            @And({
                    @Spec(path = "status", params = "status", spec = Equal.class)
            }) @Parameter(hidden = true) Specification<LibraryEntry> spec,
            @ParameterObject @PageableDefault(sort = "updatedAt") Pageable pageable
    ) {
        return libraryEntryService.searchLibrary(spec, pageable);
    }

    @GetMapping("/entries/status/{status}")
    @Operation(summary = "Get library by status", description = "Filter library entries by reading status.")
    @ApiResponse(responseCode = "200", description = "Library entries retrieved")
    public Page<LibraryEntryResponse> getByStatus(
            @PathVariable ReadingStatus status,
            @ParameterObject @PageableDefault(sort = "updatedAt") Pageable pageable
    ) {
        return libraryEntryService.getMyLibraryByStatus(status, pageable);
    }

    @GetMapping("/users/{userId}/entries")
    @Operation(summary = "Get user's public library", description = "View another user's library entries.")
    @ApiResponse(responseCode = "200", description = "Library entries retrieved")
    public Page<LibraryEntryResponse> getUserLibrary(
            @PathVariable UUID userId,
            @ParameterObject @PageableDefault(sort = "updatedAt") Pageable pageable
    ) {
        return libraryEntryService.getUserLibrary(userId, pageable);
    }

}
