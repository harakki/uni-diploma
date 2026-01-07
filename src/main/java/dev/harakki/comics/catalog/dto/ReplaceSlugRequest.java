package dev.harakki.comics.catalog.dto;

import jakarta.validation.constraints.NotBlank;

public record ReplaceSlugRequest(
        @NotBlank String slug
) {
}
