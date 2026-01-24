package dev.harakki.comics.analytics.application;

import dev.harakki.comics.analytics.api.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsEventListener {

    private final AnalyticsService analyticsService;

    @Async
    @ApplicationModuleListener
    public void on(ChapterReadEvent event) {
        log.debug("Processing chapter read event: titleId={}, userId={}, chapterId={}, readTime={}ms",
                event.titleId(), event.userId(), event.chapterId(), event.readTimeMillis());

        try {
            analyticsService.recordChapterRead(event);
            log.info("Chapter read event processed successfully: titleId={}, userId{}", event.titleId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process chapter read event: titleId={}, userId={}", event.titleId(), event.userId(), e);
        }
    }

    @Async
    @ApplicationModuleListener
    public void on(TitleLikedEvent event) {
        log.debug("Processing title liked event: titleId={}, userId={}", event.titleId(), event.userId());

        try {
            analyticsService.recordTitleLike(event);
            log.info("Title liked event processed successfully: titleId={}, userId={}", event.titleId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process title liked event: titleId={}, userId={}", event.titleId(), event.userId(), e);
        }
    }

    @Async
    @ApplicationModuleListener
    public void on(TitleDislikedEvent event) {
        log.debug("Processing title disliked event: titleId={}, userId={}", event.titleId(), event.userId());

        try {
            analyticsService.recordTitleDislike(event);
            log.info("Title disliked event processed successfully: titleId={}, userId={}", event.titleId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process title disliked event: titleId={}, userId={}", event.titleId(), event.userId(), e);
        }
    }

    @Async
    @ApplicationModuleListener
    public void on(TitleViewedEvent event) {
        log.debug("Processing title viewed event: titleId={}, userId={}", event.titleId(), event.userId());

        try {
            analyticsService.recordTitleView(event);
            log.info("Title viewed event processed successfully: titleId={}, userId={}", event.titleId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process title viewed event: titleId={}, userId={}", event.titleId(), event.userId(), e);
        }
    }


    @Async
    @ApplicationModuleListener
    public void on(TitleAddToLibraryEvent event) {
        log.debug("Processing title add to library event: titleId={}, userId={}", event.titleId(), event.userId());

        try {
            analyticsService.recordTitleAddToLibrary(event);
            log.info("Title add to library event processed successfully: titleId={}, userId={}", event.titleId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process title add to library event: titleId={}, userId={}", event.titleId(), event.userId(), e);
        }
    }

    @Async
    @ApplicationModuleListener
    public void on(TitleRemoveFromLibraryEvent event) {
        log.debug("Processing title remove from library event: titleId={}, userId={}", event.titleId(), event.userId());

        try {
            analyticsService.recordTitleRemoveFromLibrary(event);
            log.info("Title remove from library event processed successfully: titleId={}, userId={}", event.titleId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process title remove from library event: titleId={}, userId={}", event.titleId(), event.userId(), e);
        }
    }

}
