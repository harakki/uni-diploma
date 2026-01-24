package dev.harakki.comics.content.dto;

import java.io.Serializable;
import java.util.UUID;

public record NextChapterResponse(
        UUID chapterId,
        String displayNumber,
        String name,
        boolean hasNextChapter
) implements Serializable {

    public static NextChapterResponse noChapter() {
        return new NextChapterResponse(null, null, null, false);
    }

}
