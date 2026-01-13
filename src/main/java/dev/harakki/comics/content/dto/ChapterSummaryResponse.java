package dev.harakki.comics.content.dto;

import dev.harakki.comics.content.domain.Chapter;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link Chapter}
 */
public record ChapterSummaryResponse(
        UUID id,
        String displayNumber,
        String name,
        Integer volume
) implements Serializable {
}
