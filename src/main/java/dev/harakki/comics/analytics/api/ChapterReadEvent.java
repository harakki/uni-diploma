package dev.harakki.comics.analytics.api;

import java.io.Serializable;
import java.util.UUID;

public record ChapterReadEvent(
        UUID titleId,
        UUID userId,
        UUID chapterId,
        long readTimeMillis
) implements Serializable {
}
