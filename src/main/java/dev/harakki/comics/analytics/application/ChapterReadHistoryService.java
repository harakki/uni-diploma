package dev.harakki.comics.analytics.application;

import dev.harakki.comics.analytics.domain.InteractionType;
import dev.harakki.comics.analytics.infrastructure.UserInteractionRepository;
import dev.harakki.comics.shared.api.ChapterReadHistoryProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChapterReadHistoryService implements ChapterReadHistoryProvider {

    private final UserInteractionRepository interactionRepository;

    @Override
    public boolean isChapterRead(UUID userId, UUID chapterId) {
        return interactionRepository.existsByUserIdAndTargetIdAndType(userId, chapterId, InteractionType.CHAPTER_READ);
    }

    @Override
    public Set<UUID> getReadChapterIds(UUID userId, List<UUID> chapterIds) {
        if (chapterIds.isEmpty()) {
            return Set.of();
        }
        List<UUID> readIds = interactionRepository.findReadChapterIds(userId, chapterIds, InteractionType.CHAPTER_READ);
        return new HashSet<>(readIds);
    }

}
