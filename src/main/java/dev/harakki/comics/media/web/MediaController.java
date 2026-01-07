package dev.harakki.comics.media.web;

import dev.harakki.comics.media.dto.MediaUploadUrlRequest;
import dev.harakki.comics.media.dto.MediaUploadUrlResponse;
import dev.harakki.comics.media.application.MediaService;
import dev.harakki.comics.shared.api.ApiProblemResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@ApiProblemResponses
class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload-url")
    @Operation(
            summary = "Generate Upload Presigned URL",
            description = "Get a temporary URL to upload a file directly to S3 storage. Client should perform a PUT request to the returned 'url'."
    )
    @ApiResponse(responseCode = "200", description = "Presigned URL generated", content = @Content(schema = @Schema(implementation = MediaUploadUrlResponse.class)))
    public MediaUploadUrlResponse createMedia(@RequestBody @Valid MediaUploadUrlRequest request) {
        return mediaService.getUploadUrl(request.originalFilename(), request.contentType(), request.width(), request.height());
    }

    @GetMapping("/{id}/url")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get Download Presigned URL",
            description = "Generate a temporary public URL to access a private media file."
    )
    @ApiResponse(responseCode = "200", description = "URL retrieved", content = @Content(schema = @Schema(type = "string", example = "https://s3.aws.com/bucket/uploads/uuid/cover.jpg?signature=...")))
    public String getMediaUrl(@PathVariable @NotNull UUID id) {
        return mediaService.getPublicUrl(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete media", description = "Remove media record from DB and schedule S3 deletion.")
    @ApiResponse(responseCode = "204", description = "Media deleted")
    public void deleteMedia(@PathVariable @NotNull UUID id) {
        mediaService.deleteMediaById(id);
    }

}
