package dev.harakki.comics.media.dto;

import dev.harakki.comics.media.domain.Media;
import lombok.Builder;

import java.io.Serializable;
import java.util.UUID;

/**
 * Presigned URLs for media files.
 * <br><br>
 * DTO for {@link Media}.
 *
 * @param id    the ID of the media
 * @param url   the presigned URL for the media
 * @param s3Key the S3 key of the media
 */
@Builder
public record MediaUploadUrlResponse(
        UUID id,
        String url,
        String s3Key
) implements Serializable {
}
