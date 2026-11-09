package com.foresight.inventory.controller;

import com.foresight.inventory.dto.ChartDataDto;
import com.foresight.inventory.dto.DashboardStatsDto;
import com.foresight.inventory.dto.ForesightRecommendationDto;
import com.foresight.inventory.service.ForesightAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsApiController {

    private final ForesightAnalyticsService analyticsService;

    public AnalyticsApiController(ForesightAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getStats() {
        return ResponseEntity.ok(analyticsService.getDashboardStats());
    }

    @GetMapping("/charts")
    public ResponseEntity<ChartDataDto> getCharts(@RequestParam(defaultValue = "weekly") String timeframe) {
        return ResponseEntity.ok(analyticsService.getChartData(timeframe));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<ForesightRecommendationDto>> getSuggestions() {
        return ResponseEntity.ok(analyticsService.getForesightRecommendations());
    }
}
