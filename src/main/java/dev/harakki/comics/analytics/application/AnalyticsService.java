package dev.harakki.comics.analytics.application;

import dev.harakki.comics.analytics.api.ChapterReadEvent;
import dev.harakki.comics.analytics.domain.TitleRating;
import dev.harakki.comics.analytics.domain.TitleReadTime;
import dev.harakki.comics.analytics.domain.TitleView;
import dev.harakki.comics.analytics.infrastructure.TitleRatingRepository;
import dev.harakki.comics.analytics.infrastructure.TitleReadTimeRepository;
import dev.harakki.comics.analytics.infrastructure.TitleViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final TitleViewRepository titleViewRepository;
    private final TitleRatingRepository titleRatingRepository;
    private final TitleReadTimeRepository titleReadTimeRepository;

    @Transactional
    public void recordTitleView(UUID titleId, UUID userId) {
        LocalDate today = LocalDate.now();

        var existingView = titleViewRepository.findByTitleIdAndUserIdAndViewDate(titleId, userId, today);
        if (existingView.isPresent()) {
            log.debug("Title {} already viewed by user {} today, skipping", titleId, userId);
            return;
        }

        TitleView view = titleViewRepository.findByTitleIdAndUserIdAndViewDate(titleId, userId, today)
                .orElse(TitleView.builder()
                        .titleId(titleId)
                        .userId(userId)
                        .viewCount(0L)
                        .viewDate(today)
                        .build());

        view.setViewCount(view.getViewCount() + 1);
        titleViewRepository.save(view);

        log.info("Recorded title view: titleId={}, userId={}, totalViews={}", titleId, userId, view.getViewCount());
    }

    @Transactional
    public void recordTitleRating(UUID titleId, UUID userId, Integer rating) {
        if (rating < 1 || rating > 10) {
            log.warn("Invalid rating value: {}, expected 1-10", rating);
            return;
        }

        var existingRating = titleRatingRepository.findByTitleIdAndUserId(titleId, userId);

        TitleRating titleRating = existingRating.orElse(
                TitleRating.builder()
                        .titleId(titleId)
                        .userId(userId)
                        .build()
        );

        titleRating.setRating(rating);
        titleRatingRepository.save(titleRating);

        log.info("Recorded title rating: titleId={}, userId={}, rating={}", titleId, userId, rating);
    }

    @Transactional
    public void recordReadTime(ChapterReadEvent event) {
        var existingReadTime = titleReadTimeRepository.findByTitleIdAndUserId(event.titleId(), event.userId());

        TitleReadTime readTime = existingReadTime.orElse(
                TitleReadTime.builder()
                        .titleId(event.titleId())
                        .userId(event.userId())
                        .totalReadTimeMillis(0L)
                        .build()
        );

        long newTotalTime = readTime.getTotalReadTimeMillis() + event.readTimeMillis();

        readTime.setTotalReadTimeMillis(newTotalTime);

        titleReadTimeRepository.save(readTime);

        log.info("Recorded read time: titleId={}, userId={}, chapterReadTime={}ms",
                event.titleId(), event.userId(), event.readTimeMillis());
    }

    public Double getAverageRatingForTitle(UUID titleId) {
        return titleRatingRepository.getAverageRatingForTitle(titleId);
    }

    public Long getTotalViewCount(UUID titleId) {
        return titleViewRepository.getTotalViewCountForTitle(titleId);
    }

}
