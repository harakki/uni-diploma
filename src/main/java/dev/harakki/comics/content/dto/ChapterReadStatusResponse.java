package dev.harakki.comics.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.UUID;

@Schema(description = "Chapter read status response")
public record ChapterReadStatusResponse(

        @Schema(description = "Chapter unique identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID chapterId,

        @Schema(description = "Status chapter is read", example = "true")
        boolean isRead

) implements Serializable {
}
