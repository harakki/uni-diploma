package dev.harakki.comics.analytics.api;

import dev.harakki.comics.library.api.VoteType;

import java.io.Serializable;
import java.util.UUID;

public record TitleVoteEvent(
        UUID titleId,
        UUID userId,
        VoteType vote
) implements Serializable {
}
