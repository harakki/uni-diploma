package dev.harakki.comics.analytics.api;

import java.io.Serializable;
import java.util.UUID;

public record TitleRatingEvent(
        UUID titleId,
        UUID userId,
        Integer rating
) implements Serializable {
}
