package dev.harakki.comics.content.application;

import dev.harakki.comics.analytics.api.ChapterReadEvent;
import dev.harakki.comics.analytics.api.ChapterReadHistoryProvider;
import dev.harakki.comics.content.api.ChapterCreatedEvent;
import dev.harakki.comics.content.api.ChapterDeletedEvent;
import dev.harakki.comics.content.api.ChapterUpdatedEvent;
import dev.harakki.comics.content.domain.Chapter;
import dev.harakki.comics.content.domain.Page;
import dev.harakki.comics.content.dto.*;
import dev.harakki.comics.content.infrastructure.ChapterMapper;
import dev.harakki.comics.content.infrastructure.ChapterRepository;
import dev.harakki.comics.media.api.MediaDeleteRequestedEvent;
import dev.harakki.comics.media.api.MediaFixateRequestedEvent;
import dev.harakki.comics.media.api.MediaUrlProvider;
import dev.harakki.comics.shared.exception.ResourceNotFoundException;
import dev.harakki.comics.shared.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ChapterService {

    private static final int MAX_PAGES_PER_CHAPTER = 500;

    private final ChapterRepository chapterRepository;
    private final ChapterMapper chapterMapper;

    private final MediaUrlProvider mediaUrlProvider;
    private final ChapterReadHistoryProvider chapterReadHistoryProvider;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void create(UUID titleId, ChapterCreateRequest request) {
        validatePages(request.pages());

        var chapter = Chapter.builder()
                .titleId(titleId)
                .number(request.number())
                .subNumber(request.subNumber())
                .name(request.name())
                .volume(request.volume())
                .build();

        addPagesToChapter(chapter, request.pages());

        chapterRepository.save(chapter);

        request.pages().forEach(mediaId -> eventPublisher.publishEvent(new MediaFixateRequestedEvent(mediaId)));

        // Publish analytics event
        var userId = SecurityUtils.getOptionalCurrentUserId().map(UUID::fromString).orElse(null);
        if (userId != null) {
            eventPublisher.publishEvent(new ChapterCreatedEvent(
                    chapter.getId(), 
                    titleId, 
                    userId, 
                    chapter.getDisplayNumber()
            ));
        }

        log.info("Created chapter: titleId={}, number={}.{}", titleId, request.number(), request.subNumber());
    }

    public ChapterDetailsResponse getChapterDetails(UUID chapterId) {
        var chapter = chapterRepository.findByIdWithPages(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));

        List<PageResponse> pages = chapter.getPages().stream()
                .map(page -> new PageResponse(
                        page.getId(),
                        page.getMediaId(),
                        mediaUrlProvider.getPublicUrl(page.getMediaId()),
                        page.getPageOrder()
                ))
                .toList();

        return new ChapterDetailsResponse(
                chapter.getId(),
                chapter.getTitleId(),
                chapter.getDisplayNumber(),
                chapter.getName(),
                pages
        );
    }

    @Transactional
    public void updatePages(UUID chapterId, List<UUID> newMediaIds) {
        validatePages(newMediaIds);

        var chapter = chapterRepository.findByIdWithPages(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter with id " + chapterId + " not found"));

        Set<UUID> newMediaIdsSet = new HashSet<>(newMediaIds);
        Set<UUID> oldMediaIdsSet = chapter.getPages().stream()
                .map(Page::getMediaId)
                .collect(Collectors.toSet());

        chapter.getPages().clear();
        addPagesToChapter(chapter, newMediaIds);

        chapterRepository.save(chapter);

        newMediaIds.stream()
                .filter(id -> !oldMediaIdsSet.contains(id))
                .forEach(id -> eventPublisher.publishEvent(new MediaFixateRequestedEvent(id)));

        oldMediaIdsSet.stream()
                .filter(id -> !newMediaIdsSet.contains(id))
                .forEach(id -> eventPublisher.publishEvent(new MediaDeleteRequestedEvent(id)));

        // Publish analytics event
        var userId = SecurityUtils.getOptionalCurrentUserId().map(UUID::fromString).orElse(null);
        if (userId != null) {
            eventPublisher.publishEvent(new ChapterUpdatedEvent(
                    chapter.getId(),
                    chapter.getTitleId(),
                    userId
            ));
        }

        log.info("Updated pages for chapter {}: {} total", chapterId, newMediaIds.size());
    }

    @Transactional
    public void delete(UUID chapterId) {
        var chapter = chapterRepository.findByIdWithPages(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));

        List<UUID> mediaIdsToDelete = chapter.getPages().stream()
                .map(Page::getMediaId)
                .toList();

        chapterRepository.delete(chapter);
        chapterRepository.flush();

        // Asynchronously request media deletion
        mediaIdsToDelete.forEach(id -> eventPublisher.publishEvent(new MediaDeleteRequestedEvent(id)));

        // Publish analytics event
        var userId = SecurityUtils.getOptionalCurrentUserId().map(UUID::fromString).orElse(null);
        if (userId != null) {
            eventPublisher.publishEvent(new ChapterDeletedEvent(
                    chapter.getId(),
                    chapter.getTitleId(),
                    userId
            ));
        }

        log.info("Deleted chapter {} and requested deletion of {} pages", chapterId, mediaIdsToDelete.size());
    }

    public List<ChapterSummaryResponse> getChaptersByTitle(UUID titleId) {
        return chapterRepository.findAllByTitleId(titleId).stream()
                .map(c -> new ChapterSummaryResponse(
                        c.getId(),
                        c.getDisplayNumber(),
                        c.getName(),
                        c.getVolume()
                ))
                .toList();
    }

    @Transactional
    public void updateMetadata(UUID chapterId, ChapterUpdateRequest request) {
        var chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));

        chapter = chapterMapper.partialUpdate(request, chapter);

        chapterRepository.save(chapter);

        // Publish analytics event
        var userId = SecurityUtils.getOptionalCurrentUserId().map(UUID::fromString).orElse(null);
        if (userId != null) {
            eventPublisher.publishEvent(new ChapterUpdatedEvent(
                    chapter.getId(),
                    chapter.getTitleId(),
                    userId
            ));
        }

        log.debug("Updated chapter: id={} metadata", chapterId);
    }

    @Transactional
    public void recordChapterRead(UUID chapterId, UUID titleId, ChapterReadRequest request) {
        if (!chapterRepository.existsById(chapterId)) {
            throw new ResourceNotFoundException("Chapter with id " + chapterId + " not found");
        }

        var event = new ChapterReadEvent(
                titleId,
                request.userId(),
                chapterId,
                request.readTimeMillis()
        );

        eventPublisher.publishEvent(event);
        log.info("Published chapter read event: chapterId={}, userId={}", chapterId, request.userId());
    }

    public ChapterReadStatusResponse isChapterRead(UUID chapterId, UUID titleId, UUID userId) {
        if (!chapterRepository.existsById(chapterId)) {
            throw new ResourceNotFoundException("Chapter not found");
        }

        boolean isRead = chapterReadHistoryProvider.isChapterRead(userId, chapterId);
        return new ChapterReadStatusResponse(chapterId, isRead);
    }

    public NextChapterResponse getNextUnreadChapter(UUID userId, UUID titleId) {
        List<Chapter> chapters = chapterRepository.findAllByTitleId(titleId);

        if (chapters.isEmpty()) {
            return NextChapterResponse.noChapter();
        }

        List<UUID> chapterIds = chapters.stream().map(Chapter::getId).toList();
        Set<UUID> readChapterIds = chapterReadHistoryProvider.getReadChapterIds(userId, chapterIds);

        return chapters.stream()
                .filter(c -> !readChapterIds.contains(c.getId()))
                .findFirst()
                .map(c -> new NextChapterResponse(c.getId(), c.getDisplayNumber(), c.getName(), true))
                .orElse(NextChapterResponse.noChapter());
    }

    private void validatePages(List<UUID> pages) {
        if (pages == null) {
            return;
        }
        if (pages.size() > MAX_PAGES_PER_CHAPTER) {
            throw new IllegalArgumentException("Chapter cannot have more than " + MAX_PAGES_PER_CHAPTER + " pages");
        }
        if (new HashSet<>(pages).size() != pages.size()) {
            throw new IllegalArgumentException("Duplicate media IDs are not allowed in pages list");
        }
    }

    private void addPagesToChapter(Chapter chapter, List<UUID> mediaIds) {
        for (int i = 0; i < mediaIds.size(); i++) {
            var page = Page.builder()
                    .chapter(chapter)
                    .mediaId(mediaIds.get(i))
                    .pageOrder(i)
                    .build();
            chapter.getPages().add(page);
        }
    }

}
