package dev.harakki.comics.analytics.application;

import dev.harakki.comics.analytics.api.ChapterReadEvent;
import dev.harakki.comics.analytics.api.TitleAddToLibraryEvent;
import dev.harakki.comics.analytics.api.TitleRatingEvent;
import dev.harakki.comics.analytics.api.TitleRemoveFromLibraryEvent;
import dev.harakki.comics.analytics.domain.InteractionType;
import dev.harakki.comics.analytics.domain.UserInteraction;
import dev.harakki.comics.analytics.dto.UserStatsResponse;
import dev.harakki.comics.analytics.infrastructure.UserInteractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public void recordTitleView(ChapterReadEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.TITLE_VIEWED)
                .targetId(event.titleId())
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordTitleRating(TitleRatingEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.TITLE_RATED)
                .targetId(event.titleId())
                .metadata(Map.of("rating", event.rating()))
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

}
