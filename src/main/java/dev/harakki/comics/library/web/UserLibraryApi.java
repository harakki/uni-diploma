package dev.harakki.comics.library.web;

import dev.harakki.comics.library.dto.LibraryEntryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Tag(name = "User Library", description = "Public access to user libraries")
public interface UserLibraryApi {

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
    Page<LibraryEntryResponse> getUserLibrary(
            @Parameter(description = "User UUID", required = true) UUID userId,
            @ParameterObject Pageable pageable
    );

}
