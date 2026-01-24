package dev.harakki.comics.analytics.application;

import dev.harakki.comics.analytics.api.*;
import dev.harakki.comics.analytics.domain.InteractionType;
import dev.harakki.comics.analytics.domain.UserInteraction;
import dev.harakki.comics.analytics.dto.TitleAnalyticsResponse;
import dev.harakki.comics.analytics.infrastructure.UserInteractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final UserInteractionRepository interactionRepository;

    @Transactional
    public void recordChapterRead(ChapterReadEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.CHAPTER_READ)
                .targetId(event.chapterId()) // Target -> chapterId
                .metadata(Map.of(
                        "titleId", event.titleId(),
                        "readTimeMillis", event.readTimeMillis()
                ))
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordTitleLike(TitleLikedEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.TITLE_LIKED)
                .targetId(event.titleId())
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordTitleDislike(TitleDislikedEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.TITLE_DISLIKED)
                .targetId(event.titleId())
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordTitleView(TitleViewedEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.TITLE_VIEWED)
                .targetId(event.titleId())
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordTitleAddToLibrary(TitleAddToLibraryEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.TITLE_ADDED_TO_LIBRARY)
                .targetId(event.titleId())
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordTitleRemoveFromLibrary(TitleRemoveFromLibraryEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.TITLE_REMOVED_FROM_LIBRARY)
                .targetId(event.titleId())
                .build();
        interactionRepository.save(interaction);
    }

    public Double getAverageRatingForTitle(UUID titleId) {
        return interactionRepository.getAverageRating(titleId);
    }

    public Long getTotalViewCount(UUID titleId) {
        return interactionRepository.countByTargetIdAndType(titleId, InteractionType.TITLE_VIEWED);
    }

    public TitleAnalyticsResponse getTitleAnalytics(UUID titleId) {
        var averageRating = getAverageRatingForTitle(titleId);
        var totalViews = getTotalViewCount(titleId);

        return new TitleAnalyticsResponse(
                titleId,
                averageRating,
                totalViews != null ? totalViews : 0L,
                Instant.now()
        );
    }

}
