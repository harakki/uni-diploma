package dev.harakki.comics.library.dto;

import dev.harakki.comics.library.domain.ReadingStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link dev.harakki.comics.library.domain.LibraryEntry}
 */
public record LibraryEntryUpdateRequest(
        ReadingStatus status,
        @Min(1) @Max(10) Integer rating,
        UUID lastReadChapterId
) implements Serializable {
}
