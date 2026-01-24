package dev.harakki.comics.library.dto;

import dev.harakki.comics.library.domain.ReadingStatus;
import dev.harakki.comics.library.api.VoteType;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link dev.harakki.comics.library.domain.LibraryEntry}
 */
public record LibraryEntryUpdateRequest(
        ReadingStatus status,
        VoteType vote,
        UUID lastReadChapterId
) implements Serializable {
}
