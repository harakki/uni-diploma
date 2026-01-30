package dev.harakki.comics.analytics.application;

import dev.harakki.comics.analytics.api.*;
import dev.harakki.comics.catalog.api.*;
import dev.harakki.comics.collections.api.*;
import dev.harakki.comics.content.api.*;
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

    @Async
    @ApplicationModuleListener
    public void on(TitleCreatedEvent event) {
        log.debug("Processing title created event: titleId={}, userId={}, titleName={}",
                event.titleId(), event.userId(), event.titleName());

        try {
            analyticsService.recordTitleCreated(event);
            log.info("Title created event processed successfully: titleId={}, userId={}", event.titleId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process title created event: titleId={}, userId={}", event.titleId(), event.userId(), e);
        }
    }

    @Async
    @ApplicationModuleListener
    public void on(TitleUpdatedEvent event) {
        log.debug("Processing title updated event: titleId={}, userId={}", event.titleId(), event.userId());

        try {
            analyticsService.recordTitleUpdated(event);
            log.info("Title updated event processed successfully: titleId={}, userId={}", event.titleId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process title updated event: titleId={}, userId={}", event.titleId(), event.userId(), e);
        }
    }

    @Async
    @ApplicationModuleListener
    public void on(TitleDeletedEvent event) {
        log.debug("Processing title deleted event: titleId={}, userId={}", event.titleId(), event.userId());

        try {
            analyticsService.recordTitleDeleted(event);
            log.info("Title deleted event processed successfully: titleId={}, userId={}", event.titleId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process title deleted event: titleId={}, userId={}", event.titleId(), event.userId(), e);
        }
    }

    @Async
    @ApplicationModuleListener
    public void on(AuthorCreatedEvent event) {
        log.debug("Processing author created event: authorId={}, userId={}, authorName={}",
                event.authorId(), event.userId(), event.authorName());

        try {
            analyticsService.recordAuthorCreated(event);
            log.info("Author created event processed successfully: authorId={}, userId={}", event.authorId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process author created event: authorId={}, userId={}", event.authorId(), event.userId(), e);
        }
    }

    @Async
    @ApplicationModuleListener
    public void on(AuthorUpdatedEvent event) {
        log.debug("Processing author updated event: authorId={}, userId={}", event.authorId(), event.userId());

        try {
            analyticsService.recordAuthorUpdated(event);
            log.info("Author updated event processed successfully: authorId={}, userId={}", event.authorId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process author updated event: authorId={}, userId={}", event.authorId(), event.userId(), e);
        }
    }

    @Async
    @ApplicationModuleListener
    public void on(AuthorDeletedEvent event) {
        log.debug("Processing author deleted event: authorId={}, userId={}", event.authorId(), event.userId());

        try {
            analyticsService.recordAuthorDeleted(event);
            log.info("Author deleted event processed successfully: authorId={}, userId={}", event.authorId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process author deleted event: authorId={}, userId={}", event.authorId(), event.userId(), e);
        }
    }

    @Async
    @ApplicationModuleListener
    public void on(PublisherCreatedEvent event) {
        log.debug("Processing publisher created event: publisherId={}, userId={}, publisherName={}",
                event.publisherId(), event.userId(), event.publisherName());

        try {
            analyticsService.recordPublisherCreated(event);
            log.info("Publisher created event processed successfully: publisherId={}, userId={}", event.publisherId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process publisher created event: publisherId={}, userId={}", event.publisherId(), event.userId(), e);
        }
    }

    @Async
    @ApplicationModuleListener
    public void on(PublisherUpdatedEvent event) {
        log.debug("Processing publisher updated event: publisherId={}, userId={}", event.publisherId(), event.userId());

        try {
            analyticsService.recordPublisherUpdated(event);
            log.info("Publisher updated event processed successfully: publisherId={}, userId={}", event.publisherId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process publisher updated event: publisherId={}, userId={}", event.publisherId(), event.userId(), e);
        }
    }

    @Async
    @ApplicationModuleListener
    public void on(PublisherDeletedEvent event) {
        log.debug("Processing publisher deleted event: publisherId={}, userId={}", event.publisherId(), event.userId());

        try {
            analyticsService.recordPublisherDeleted(event);
            log.info("Publisher deleted event processed successfully: publisherId={}, userId={}", event.publisherId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process publisher deleted event: publisherId={}, userId={}", event.publisherId(), event.userId(), e);
        }
    }

    @Async
    @ApplicationModuleListener
    public void on(ChapterCreatedEvent event) {
        log.debug("Processing chapter created event: chapterId={}, titleId={}, userId={}, chapterNumber={}",
                event.chapterId(), event.titleId(), event.userId(), event.chapterNumber());

        try {
            analyticsService.recordChapterCreated(event);
            log.info("Chapter created event processed successfully: chapterId={}, userId={}", event.chapterId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process chapter created event: chapterId={}, userId={}", event.chapterId(), event.userId(), e);
        }
    }

    @Async
    @ApplicationModuleListener
    public void on(ChapterUpdatedEvent event) {
        log.debug("Processing chapter updated event: chapterId={}, titleId={}, userId={}",
                event.chapterId(), event.titleId(), event.userId());

        try {
            analyticsService.recordChapterUpdated(event);
            log.info("Chapter updated event processed successfully: chapterId={}, userId={}", event.chapterId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process chapter updated event: chapterId={}, userId={}", event.chapterId(), event.userId(), e);
        }
    }

    @Async
    @ApplicationModuleListener
    public void on(ChapterDeletedEvent event) {
        log.debug("Processing chapter deleted event: chapterId={}, titleId={}, userId={}",
                event.chapterId(), event.titleId(), event.userId());

        try {
            analyticsService.recordChapterDeleted(event);
            log.info("Chapter deleted event processed successfully: chapterId={}, userId={}", event.chapterId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process chapter deleted event: chapterId={}, userId={}", event.chapterId(), event.userId(), e);
        }
    }

    @Async
    @ApplicationModuleListener
    public void on(CollectionCreatedEvent event) {
        log.debug("Processing collection created event: collectionId={}, userId={}, collectionName={}",
                event.collectionId(), event.userId(), event.collectionName());

        try {
            analyticsService.recordCollectionCreated(event);
            log.info("Collection created event processed successfully: collectionId={}, userId={}", event.collectionId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process collection created event: collectionId={}, userId={}", event.collectionId(), event.userId(), e);
        }
    }

    @Async
    @ApplicationModuleListener
    public void on(CollectionUpdatedEvent event) {
        log.debug("Processing collection updated event: collectionId={}, userId={}", event.collectionId(), event.userId());

        try {
            analyticsService.recordCollectionUpdated(event);
            log.info("Collection updated event processed successfully: collectionId={}, userId={}", event.collectionId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process collection updated event: collectionId={}, userId={}", event.collectionId(), event.userId(), e);
        }
    }

    @Async
    @ApplicationModuleListener
    public void on(CollectionDeletedEvent event) {
        log.debug("Processing collection deleted event: collectionId={}, userId={}", event.collectionId(), event.userId());

        try {
            analyticsService.recordCollectionDeleted(event);
            log.info("Collection deleted event processed successfully: collectionId={}, userId={}", event.collectionId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process collection deleted event: collectionId={}, userId={}", event.collectionId(), event.userId(), e);
        }
    }

    @Async
    @ApplicationModuleListener
    public void on(CollectionTitleAddedEvent event) {
        log.debug("Processing title added to collection event: collectionId={}, titleId={}, userId={}",
                event.collectionId(), event.titleId(), event.userId());

        try {
            analyticsService.recordCollectionTitleAdded(event);
            log.info("Title added to collection event processed successfully: titleId={}, userId={}", event.titleId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process title added to collection event: titleId={}, userId={}", event.titleId(), event.userId(), e);
        }
    }

    @Async
    @ApplicationModuleListener
    public void on(CollectionTitleRemovedEvent event) {
        log.debug("Processing title removed from collection event: collectionId={}, titleId={}, userId={}",
                event.collectionId(), event.titleId(), event.userId());

        try {
            analyticsService.recordCollectionTitleRemoved(event);
            log.info("Title removed from collection event processed successfully: titleId={}, userId={}", event.titleId(), event.userId());
        } catch (Exception e) {
            log.error("Failed to process title removed from collection event: titleId={}, userId={}", event.titleId(), event.userId(), e);
        }
    }

}
