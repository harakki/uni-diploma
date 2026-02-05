package dev.harakki.comics.analytics.web;

import dev.harakki.comics.analytics.application.AnalyticsService;
import dev.harakki.comics.analytics.dto.TitleAnalyticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/analytics", produces = MediaType.APPLICATION_JSON_VALUE)
public class AnalyticsController implements AnalyticsApi {

    private final AnalyticsService analyticsService;

    @GetMapping("/titles/{titleId}")
    public TitleAnalyticsResponse getTitleAnalytics(@PathVariable UUID titleId) {
        return analyticsService.getTitleAnalytics(titleId);
    }

}
