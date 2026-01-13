package dev.harakki.comics.content.web;

import dev.harakki.comics.content.application.ChapterService;
import dev.harakki.comics.content.dto.*;
import dev.harakki.comics.shared.api.ApiProblemResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Chapters", description = "Management of comic chapters.")
@ApiProblemResponses
public class ChapterController {

    private final ChapterService chapterService;

    @PostMapping("/titles/{titleId}/chapters")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create chapter", description = "Create a chapter and link uploaded pages to it.")
    public void createChapter(
            @PathVariable UUID titleId,
            @RequestBody @Valid ChapterCreateRequest request
    ) {
        chapterService.create(titleId, request);
    }

    @GetMapping("/titles/{titleId}/chapters")
    @Operation(summary = "Get title chapters", description = "List all chapters for a title (without pages).")
    public List<ChapterSummaryResponse> getTitleChapters(@PathVariable UUID titleId) {
        return chapterService.getChaptersByTitle(titleId);
    }

    @GetMapping("/chapters/{chapterId}")
    @Operation(summary = "Get specific chapter content", description = "Get chapter metadata and all page URLs.")
    public ChapterDetailsResponse getChapterDetails(@PathVariable UUID chapterId) {
        return chapterService.getChapterDetails(chapterId);
    }

    @PutMapping("/chapters/{chapterId}")
    @Operation(summary = "Update chapter info", description = "Update number, name or volume.")
    public void updateChapter(
            @PathVariable UUID chapterId,
            @RequestBody @Valid ChapterUpdateRequest request
    ) {
        chapterService.updateMetadata(chapterId, request);
    }

    @DeleteMapping("/chapters/{chapterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete chapter", description = "Delete chapter and all associated pages.")
    public void deleteChapter(@PathVariable UUID chapterId) {
        chapterService.delete(chapterId);
    }

    @PutMapping("/chapters/{chapterId}/pages")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Update pages order/content",
            description = "Full replacement of pages list. Used for reordering, adding or deleting pages for a chapter.")
    public void updatePages(
            @PathVariable UUID chapterId,
            @RequestBody @Valid ChapterPagesUpdateRequest request
    ) {
        chapterService.updatePages(chapterId, request.pages());
    }

}
