package dev.harakki.comics.content.application;

import dev.harakki.comics.content.domain.Chapter;
import dev.harakki.comics.content.domain.Page;
import dev.harakki.comics.content.dto.*;
import dev.harakki.comics.content.infrastructure.ChapterMapper;
import dev.harakki.comics.content.infrastructure.ChapterRepository;
import dev.harakki.comics.media.api.MediaDeleteRequestedEvent;
import dev.harakki.comics.media.api.MediaFixateRequestedEvent;
import dev.harakki.comics.media.api.MediaUrlProvider;
import dev.harakki.comics.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChapterService {

    private final ChapterRepository chapterRepository;
    private final ChapterMapper chapterMapper;

    private final MediaUrlProvider mediaUrlProvider;
    private final ApplicationEventPublisher events;

    @Transactional
    public void create(UUID titleId, ChapterCreateRequest request) {
        var chapter = Chapter.builder()
                .titleId(titleId)
                .number(request.number())
                .subNumber(request.subNumber())
                .name(request.name())
                .volume(request.volume())
                .build();

        addPagesToChapter(chapter, request.pages());

        chapterRepository.save(chapter);

        // Fixate media asynchronously
        request.pages().forEach(mediaId -> events.publishEvent(new MediaFixateRequestedEvent(mediaId)));

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
        var chapter = chapterRepository.findByIdWithPages(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));

        List<UUID> oldMediaIds = chapter.getPages().stream()
                .map(Page::getMediaId)
                .toList();

        Set<UUID> oldSet = new HashSet<>(oldMediaIds);
        Set<UUID> newSet = new HashSet<>(newMediaIds);

        List<UUID> toDelete = new ArrayList<>(oldMediaIds);
        toDelete.removeIf(newSet::contains);  // Delete only old media

        List<UUID> toFixate = new ArrayList<>(newMediaIds);
        toFixate.removeIf(oldSet::contains); // Fixate only new media

        chapter.getPages().clear();
        addPagesToChapter(chapter, newMediaIds);
        chapterRepository.save(chapter);

        // Asynchronously request media fixate and delete
        toFixate.forEach(id -> events.publishEvent(new MediaFixateRequestedEvent(id)));
        toDelete.forEach(id -> events.publishEvent(new MediaDeleteRequestedEvent(id)));

        log.info("Updated pages: {} added, {} removed", toFixate.size(), toDelete.size());
    }

    @Transactional
    public void delete(UUID chapterId) {
        var chapter = chapterRepository.findByIdWithPages(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found"));

        List<UUID> mediaIdsToDelete = chapter.getPages().stream()
                .map(Page::getMediaId)
                .toList();

        chapterRepository.delete(chapter);

        // Asynchronously request media deletion
        mediaIdsToDelete.forEach(id -> events.publishEvent(new MediaDeleteRequestedEvent(id)));

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
        log.debug("Updated chapter: id={} metadata", chapterId);
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
