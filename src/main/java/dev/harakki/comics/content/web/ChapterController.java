package dev.harakki.comics.content.web;

import dev.harakki.comics.content.application.ChapterService;
import dev.harakki.comics.content.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChapterController implements ChapterApi {

    private final ChapterService chapterService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/titles/{titleId}/chapters")
    @ResponseStatus(HttpStatus.CREATED)
    public void createChapter(
            @PathVariable UUID titleId,
            @RequestBody @Valid ChapterCreateRequest request
    ) {
        chapterService.create(titleId, request);
    }

    @GetMapping("/titles/{titleId}/chapters")
    public List<ChapterSummaryResponse> getTitleChapters(@PathVariable UUID titleId) {
        return chapterService.getChaptersByTitle(titleId);
    }

    @GetMapping("/chapters/{chapterId}")
    public ChapterDetailsResponse getChapterDetails(@PathVariable UUID chapterId) {
        return chapterService.getChapterDetails(chapterId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/chapters/{chapterId}")
    public void updateChapter(
            @PathVariable UUID chapterId,
            @RequestBody @Valid ChapterUpdateRequest request
    ) {
        chapterService.updateMetadata(chapterId, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/chapters/{chapterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChapter(@PathVariable UUID chapterId) {
        chapterService.delete(chapterId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/chapters/{chapterId}/pages")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePages(
            @PathVariable UUID chapterId,
            @RequestBody @Valid ChapterPagesUpdateRequest request
    ) {
        chapterService.updatePages(chapterId, request.pages());
    }

}
