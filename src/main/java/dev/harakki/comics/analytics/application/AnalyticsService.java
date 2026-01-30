package dev.harakki.comics.analytics.application;

import dev.harakki.comics.analytics.api.TitleDislikedEvent;
import dev.harakki.comics.analytics.api.TitleLikedEvent;
import dev.harakki.comics.analytics.domain.InteractionType;
import dev.harakki.comics.analytics.domain.UserInteraction;
import dev.harakki.comics.analytics.dto.TitleAnalyticsResponse;
import dev.harakki.comics.analytics.infrastructure.UserInteractionRepository;
import dev.harakki.comics.catalog.api.*;
import dev.harakki.comics.collections.api.*;
import dev.harakki.comics.content.api.ChapterCreatedEvent;
import dev.harakki.comics.content.api.ChapterDeletedEvent;
import dev.harakki.comics.content.api.ChapterReadEvent;
import dev.harakki.comics.content.api.ChapterUpdatedEvent;
import dev.harakki.comics.library.api.LibraryAddTitleEvent;
import dev.harakki.comics.library.api.LibraryRemoveTitleEvent;
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
    public void recordTitleAddToLibrary(LibraryAddTitleEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.TITLE_ADDED_TO_LIBRARY)
                .targetId(event.titleId())
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordTitleRemoveFromLibrary(LibraryRemoveTitleEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.TITLE_REMOVED_FROM_LIBRARY)
                .targetId(event.titleId())
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordTitleCreated(TitleCreatedEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.TITLE_CREATED)
                .targetId(event.titleId())
                .metadata(Map.of("titleName", event.titleName()))
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordTitleUpdated(TitleUpdatedEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.TITLE_UPDATED)
                .targetId(event.titleId())
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordTitleDeleted(TitleDeletedEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.TITLE_DELETED)
                .targetId(event.titleId())
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordAuthorCreated(AuthorCreatedEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.AUTHOR_CREATED)
                .targetId(event.authorId())
                .metadata(Map.of("authorName", event.authorName()))
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordAuthorUpdated(AuthorUpdatedEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.AUTHOR_UPDATED)
                .targetId(event.authorId())
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordAuthorDeleted(AuthorDeletedEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.AUTHOR_DELETED)
                .targetId(event.authorId())
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordPublisherCreated(PublisherCreatedEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.PUBLISHER_CREATED)
                .targetId(event.publisherId())
                .metadata(Map.of("publisherName", event.publisherName()))
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordPublisherUpdated(PublisherUpdatedEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.PUBLISHER_UPDATED)
                .targetId(event.publisherId())
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordPublisherDeleted(PublisherDeletedEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.PUBLISHER_DELETED)
                .targetId(event.publisherId())
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordChapterCreated(ChapterCreatedEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.CHAPTER_CREATED)
                .targetId(event.chapterId())
                .metadata(Map.of(
                        "titleId", event.titleId(),
                        "chapterNumber", event.chapterNumber()
                ))
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordChapterUpdated(ChapterUpdatedEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.CHAPTER_UPDATED)
                .targetId(event.chapterId())
                .metadata(Map.of("titleId", event.titleId()))
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordChapterDeleted(ChapterDeletedEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.CHAPTER_DELETED)
                .targetId(event.chapterId())
                .metadata(Map.of("titleId", event.titleId()))
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordCollectionCreated(CollectionCreatedEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.COLLECTION_CREATED)
                .targetId(event.collectionId())
                .metadata(Map.of("collectionName", event.collectionName()))
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordCollectionUpdated(CollectionUpdatedEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.COLLECTION_UPDATED)
                .targetId(event.collectionId())
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordCollectionDeleted(CollectionDeletedEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.COLLECTION_DELETED)
                .targetId(event.collectionId())
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordCollectionTitleAdded(CollectionTitleAddedEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.TITLE_ADDED_TO_COLLECTION)
                .targetId(event.titleId())
                .metadata(Map.of("collectionId", event.collectionId()))
                .build();
        interactionRepository.save(interaction);
    }

    @Transactional
    public void recordCollectionTitleRemoved(CollectionTitleRemovedEvent event) {
        var interaction = UserInteraction.builder()
                .userId(event.userId())
                .type(InteractionType.TITLE_REMOVED_FROM_COLLECTION)
                .targetId(event.titleId())
                .metadata(Map.of("collectionId", event.collectionId()))
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
