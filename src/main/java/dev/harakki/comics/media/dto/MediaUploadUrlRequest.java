package dev.harakki.comics.media.dto;

import dev.harakki.comics.media.domain.Media;
import jakarta.validation.constraints.*;

import java.io.Serializable;

/**
 * Upload URL request for media files.
 * <br><br>
 * DTO for {@link Media}.
 *
 * @param originalFilename the original filename of the media
 * @param contentType      the content type of the media
 * @param width            the width of the media
 * @param height           the height of the media
 */
public record MediaUploadUrlRequest(
        @NotBlank @Size(min=1, max=255) String originalFilename,
        @NotBlank String contentType,
        @NotNull @Positive @Max(4100) Integer width,
        @NotNull @Positive Integer height
) implements Serializable {
}
