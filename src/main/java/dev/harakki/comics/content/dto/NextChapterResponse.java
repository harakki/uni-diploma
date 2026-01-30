package dev.harakki.comics.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.UUID;

@Schema(description = "Response containing next chapter information")
public record NextChapterResponse(

        @Schema(description = "Next chapter unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID chapterId,

        @Schema(description = "Next chapter display number", example = "5.1")
        String displayNumber,

        @Schema(description = "Next chapter name", example = "The Journey Continues")
        String name,

        @Schema(description = "Indicates if there is a next chapter available", example = "true")
        boolean hasNextChapter

) implements Serializable {

    public static NextChapterResponse noChapter() {
        return new NextChapterResponse(null, null, null, false);
    }

}
