package dev.harakki.comics.catalog.dto;

import dev.harakki.comics.catalog.domain.TagType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record TagCreateRequest(
        @NotBlank String name,
        String slug,
        @NotNull TagType type,
        String description
) {
}
