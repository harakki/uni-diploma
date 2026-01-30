package dev.harakki.comics.content.api;

import java.io.Serializable;
import java.util.UUID;

public record ChapterUpdatedEvent(
        UUID chapterId,
        UUID titleId,
        UUID userId
) implements Serializable {
}
