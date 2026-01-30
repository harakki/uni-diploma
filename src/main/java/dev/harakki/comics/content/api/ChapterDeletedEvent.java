package dev.harakki.comics.content.api;

import java.io.Serializable;
import java.util.UUID;

public record ChapterDeletedEvent(
        UUID chapterId,
        UUID titleId,
        UUID userId
) implements Serializable {
}
