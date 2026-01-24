package dev.harakki.comics.collections.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link dev.harakki.comics.collections.domain.Collection}
 */
public record UserCollectionResponse(
        UUID id,
        UUID authorId,
        String name,
        String description,
        Boolean isPublic,
        String shareToken,
        List<UUID> titleIds,
        Instant createdAt,
        Instant updatedAt
) implements Serializable {
}
