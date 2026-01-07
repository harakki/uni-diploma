package dev.harakki.comics.catalog.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;
import java.util.UUID;

public record ReplaceTagsRequest(
        @NotEmpty Set<UUID> tagIds
) {
}
