package dev.harakki.comics.analytics.api;

import java.io.Serializable;
import java.util.UUID;

public record TitleLikedEvent(
        UUID titleId,
        UUID userId
) implements Serializable {
}
