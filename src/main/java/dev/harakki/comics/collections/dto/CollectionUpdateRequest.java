package dev.harakki.comics.collections.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link dev.harakki.comics.collections.domain.Collection}
 */
public record CollectionUpdateRequest(
        String name,
        String description,
        Boolean isPublic,
        List<UUID> titleIds
) implements Serializable {
}
