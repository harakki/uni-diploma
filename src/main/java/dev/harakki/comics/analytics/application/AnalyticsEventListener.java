package dev.harakki.comics.analytics.application;

import dev.harakki.comics.analytics.api.ChapterReadEvent;
import dev.harakki.comics.analytics.api.TitleRatingEvent;
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
            // Increment title view
            analyticsService.recordTitleView(event.titleId(), event.userId());

            // Update read time
            analyticsService.recordReadTime(event);

            log.info("Chapter read event processed successfully: titleId={}, userId{}",
                    event.titleId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process chapter read event: titleId={}, userId={}", event.titleId(), event.userId(), e);
        }
    }

    @Async
    @ApplicationModuleListener
    public void on(TitleRatingEvent event) {
        log.debug("Processing title rating event: titleId={}, userId={}, rating={}", event.titleId(), event.userId(), event.rating());

        try {
            analyticsService.recordTitleRating(event.titleId(), event.userId(), event.rating());
            log.info("Title rating event processed successfully: titleId={}, userId={}", event.titleId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process title rating event: titleId={}, userId={}", event.titleId(), event.userId(), e);
        }
    }

}
