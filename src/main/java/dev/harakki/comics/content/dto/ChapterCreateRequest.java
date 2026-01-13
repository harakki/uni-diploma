package dev.harakki.comics.content.dto;

import dev.harakki.comics.content.domain.Chapter;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link Chapter}
 */
public record ChapterCreateRequest(
        @NotNull Integer number,
        @NotNull @PositiveOrZero Integer subNumber,
        String name,
        Integer volume,
        @NotEmpty List<UUID> pages
) implements Serializable {
}
