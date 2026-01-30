package dev.harakki.comics.content.api;

import java.io.Serializable;
import java.util.UUID;

public record ChapterCreatedEvent(
        UUID chapterId,
        UUID titleId,
        UUID userId,
        String chapterNumber
) implements Serializable {
}
