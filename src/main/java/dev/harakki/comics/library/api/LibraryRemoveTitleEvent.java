package dev.harakki.comics.library.api;

import java.io.Serializable;
import java.util.UUID;

public record LibraryRemoveTitleEvent(
        UUID titleId,
        UUID userId
) implements Serializable {
}
