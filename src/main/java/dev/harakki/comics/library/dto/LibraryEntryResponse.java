package dev.harakki.comics.library.dto;

import dev.harakki.comics.library.api.VoteType;
import dev.harakki.comics.library.domain.ReadingStatus;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link dev.harakki.comics.library.domain.LibraryEntry}
 */
public record LibraryEntryResponse(
        UUID id,
        UUID userId,
        UUID titleId,
        ReadingStatus status,
        VoteType vote,
        UUID lastReadChapterId,
        Instant createdAt,
        Instant updatedAt
) implements Serializable {
}
