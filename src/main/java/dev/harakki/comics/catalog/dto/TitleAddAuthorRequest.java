package dev.harakki.comics.catalog.dto;

import dev.harakki.comics.catalog.domain.AuthorRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TitleAddAuthorRequest(
    @NotNull UUID authorId,
    @NotNull AuthorRole role
) {
}
