package dev.harakki.comics.content.dto;

import java.io.Serializable;
import java.util.UUID;

public record ChapterReadStatusResponse(
        UUID chapterId,
        boolean isRead
) implements Serializable {
}
