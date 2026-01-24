package dev.harakki.comics.collections.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link dev.harakki.comics.collections.domain.Collection}
 */
public record CollectionCreateRequest(
        @NotBlank String name,
        String description,
        @NotNull Boolean isPublic,
        List<UUID> titleIds
) implements Serializable {
}
