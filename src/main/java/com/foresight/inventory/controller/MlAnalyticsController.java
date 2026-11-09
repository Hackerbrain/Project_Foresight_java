package com.foresight.inventory.controller;

import com.foresight.inventory.service.MlAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics/ml")
public class MlAnalyticsController {

    private final MlAnalyticsService mlAnalyticsService;

    public MlAnalyticsController(MlAnalyticsService mlAnalyticsService) {
        this.mlAnalyticsService = mlAnalyticsService;
    }

    @GetMapping("/eda")
    public ResponseEntity<Map<String, Object>> getEdaData() {
        return ResponseEntity.ok(mlAnalyticsService.getEdaData());
    }

    @GetMapping("/features")
    public ResponseEntity<Map<String, Object>> getFeatureEngineeringData() {
        return ResponseEntity.ok(mlAnalyticsService.getFeatureEngineeringData());
    }

    @GetMapping("/forecasting")
    public ResponseEntity<Map<String, Object>> getForecastingData() {
        return ResponseEntity.ok(mlAnalyticsService.getForecastingAndModelData());
    }
}
