package dev.harakki.comics.analytics.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link dev.harakki.comics.analytics.domain.TitleReadTime}
 */
public record TitleReadTimeResponse(
        UUID id,
        UUID titleId,
        UUID userId,
        Long totalReadTimeMillis,
        Instant createdAt,
        Instant updatedAt
) implements Serializable {
}
