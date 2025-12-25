package dev.harakki.comics.media.api;

import java.io.Serializable;
import java.util.UUID;

public record MediaDeleteRequestedEvent(
        UUID mediaId
) implements Serializable {
}
