package dev.harakki.comics.library.web;

import dev.harakki.comics.library.application.LibraryService;
import dev.harakki.comics.library.dto.LibraryEntryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = UserLibraryController.REQUEST_MAPPING, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "User Library", description = "Public access to user libraries")
class UserLibraryController {

    static final String REQUEST_MAPPING = "/api/v1/users/{userId}/library";

    private final LibraryService libraryService;

    @Operation(
            operationId = "getUserLibrary",
            summary = "Get user's public library",
            description = "View another user's library entries."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Library entries retrieved",
                    content = @Content(schema = @Schema(implementation = LibraryEntryResponse.class))),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    @GetMapping
    public Page<LibraryEntryResponse> getUserLibrary(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId,
            @ParameterObject @PageableDefault(sort = "updatedAt") Pageable pageable
    ) {
        return libraryService.getUserLibrary(userId, pageable);
    }

}
