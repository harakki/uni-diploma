package dev.harakki.comics.content.dto;

import dev.harakki.comics.content.domain.Chapter;

import java.io.Serializable;

/**
 * DTO for {@link Chapter}
 */
public record ChapterUpdateRequest(
        Integer number,
        Integer subNumber,
        String name,
        Integer volume
) implements Serializable {
}
