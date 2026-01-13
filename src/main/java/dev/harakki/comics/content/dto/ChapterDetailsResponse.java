package dev.harakki.comics.content.dto;

import dev.harakki.comics.content.domain.Chapter;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link Chapter}
 */
public record ChapterDetailsResponse(
        UUID id,
        UUID titleId,
        String displayNumber,
        String name,
        List<PageResponse> pages
) implements Serializable {
}
