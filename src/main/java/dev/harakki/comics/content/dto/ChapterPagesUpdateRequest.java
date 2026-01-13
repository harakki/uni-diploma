package dev.harakki.comics.content.dto;

import dev.harakki.comics.content.domain.Chapter;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link Chapter}
 */
public record ChapterPagesUpdateRequest(
        @NotEmpty List<UUID> pages
) implements Serializable {
}
