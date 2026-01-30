package dev.harakki.comics.library.api;

import java.io.Serializable;
import java.util.UUID;

public record LibraryVoteTitleEvent(
        UUID titleId,
        UUID userId,
        VoteType vote
) implements Serializable {
}
