package dev.harakki.comics.media.web;

import dev.harakki.comics.media.application.MediaService;
import dev.harakki.comics.media.dto.MediaUploadUrlRequest;
import dev.harakki.comics.media.dto.MediaUploadUrlResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/media", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Media", description = "S3 Object Storage management via Presigned URLs.")
class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload-url")
    @Operation(
            operationId = "generateUploadUrl",
            summary = "Generate Upload Presigned URL",
            description = "Get a temporary URL to upload a file directly to S3 storage. Client should perform a PUT request to the returned 'url'."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presigned URL generated",
                    content = @Content(schema = @Schema(implementation = MediaUploadUrlResponse.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden")
    })
    public MediaUploadUrlResponse createMedia(@RequestBody @Valid MediaUploadUrlRequest request) {
        return mediaService.getUploadUrl(request.originalFilename(), request.contentType(), request.width(), request.height());
    }

    @GetMapping("/{id}/url")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            operationId = "getMediaUrl",
            summary = "Get Download Presigned URL",
            description = "Generate a temporary public URL to access a private media file."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "URL retrieved",
                    content = @Content(schema = @Schema(type = "string", example = "https://s3.aws.com/bucket/uploads/uuid/cover.jpg?signature=..."))),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public String getMediaUrl(
            @Parameter(description = "Media UUID", required = true)
            @PathVariable @NotNull UUID id
    ) {
        return mediaService.getPublicUrl(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            operationId = "deleteMedia",
            summary = "Delete media",
            description = "Remove media record from DB and schedule S3 deletion."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Media deleted"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public void deleteMedia(
            @Parameter(description = "Media UUID", required = true)
            @PathVariable @NotNull UUID id
    ) {
        mediaService.deleteMediaById(id);
    }

}
