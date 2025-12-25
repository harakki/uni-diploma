package dev.harakki.comics.media.dto;

import dev.harakki.comics.media.domain.Media;

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
public record MediaUploadUrlRequestDto(
        String originalFilename,
        String contentType,
        Integer width,
        Integer height
) implements Serializable {
}
