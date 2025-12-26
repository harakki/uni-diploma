package dev.harakki.comics.media.web;

import dev.harakki.comics.media.dto.MediaUploadUrlRequestDto;
import dev.harakki.comics.media.dto.MediaUploadUrlResponseDto;
import dev.harakki.comics.media.application.MediaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload-url")
    public MediaUploadUrlResponseDto createMedia(@RequestBody @Valid MediaUploadUrlRequestDto request) {
        return mediaService.getUploadUrl(request.originalFilename(), request.contentType(), request.width(), request.height());
    }

    @GetMapping("/{id}/url")
    @ResponseStatus(HttpStatus.OK)
    public String getMediaUrl(@PathVariable @NotNull UUID id) {
        return mediaService.getPublicUrl(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMedia(@PathVariable @NotNull UUID id) {
        mediaService.deleteMediaById(id);
    }

}
