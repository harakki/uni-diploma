package dev.harakki.comics.content.web;

import dev.harakki.comics.content.application.ChapterService;
import dev.harakki.comics.content.dto.ChapterReadRequest;
import dev.harakki.comics.content.dto.ChapterReadStatusResponse;
import dev.harakki.comics.content.dto.NextChapterResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/", produces = MediaType.APPLICATION_JSON_VALUE)
public class ReadingProgressController implements ReadingProgressApi {

    private final ChapterService chapterService;

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/titles/{titleId}/chapters/{chapterId}/read-status")
    public void recordChapterRead(
            @PathVariable UUID titleId,
            @PathVariable UUID chapterId,
            @RequestBody @Valid ChapterReadRequest request
    ) {
        chapterService.recordChapterRead(chapterId, titleId, request);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/titles/{titleId}/chapters/{chapterId}/read")
    public ChapterReadStatusResponse isChapterRead(
            @PathVariable UUID titleId,
            @PathVariable UUID chapterId
    ) {
        return chapterService.isChapterRead(chapterId, titleId);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/titles/{titleId}/next-chapter")
    public NextChapterResponse getNextUnreadChapter(@PathVariable UUID titleId) {
        return chapterService.getNextUnreadChapter(titleId);
    }

}
