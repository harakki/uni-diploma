package dev.harakki.comics.content.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ChapterReadRequest(
        @NotNull(message = "User ID is required")
        UUID userId,

        @Positive(message = "Read time must be positive")
        long readTimeMillis
) {
}
