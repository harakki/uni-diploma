package dev.harakki.comics.media.web;

import dev.harakki.comics.media.application.MediaService;
import dev.harakki.comics.media.dto.MediaUploadUrlRequest;
import dev.harakki.comics.media.dto.MediaUploadUrlResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/media", produces = MediaType.APPLICATION_JSON_VALUE)
class MediaController implements MediaApi {

    private final MediaService mediaService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/upload-url")
    public MediaUploadUrlResponse createMedia(@RequestBody @Valid MediaUploadUrlRequest request) {
        return mediaService.getUploadUrl(request.originalFilename(), request.contentType(), request.width(), request.height());
    }

    @GetMapping("/{id}" + "/url")
    @ResponseStatus(HttpStatus.OK)
    public String getMediaUrl(@PathVariable @NotNull UUID id) {
        return mediaService.getPublicUrl(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMedia(@PathVariable @NotNull UUID id) {
        mediaService.deleteMediaById(id);
    }

}
