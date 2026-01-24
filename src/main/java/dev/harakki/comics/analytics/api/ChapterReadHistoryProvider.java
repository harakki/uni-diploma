package dev.harakki.comics.analytics.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ChapterReadHistoryProvider {

    /**
     * Check if a specific chapter has been read by a user.
     *
     * @param userId    the user ID
     * @param chapterId the chapter ID
     * @return true if the chapter has been read, false otherwise
     */
    boolean isChapterRead(UUID userId, UUID chapterId);

    /**
     * Get all chapter IDs that have been read by a user for a specific title.
     *
     * @param userId     the user ID
     * @param chapterIds the list of chapter IDs to check
     * @return set of chapter IDs that have been read or empty set if none are read
     */
    Set<UUID> getReadChapterIds(UUID userId, List<UUID> chapterIds);

}
