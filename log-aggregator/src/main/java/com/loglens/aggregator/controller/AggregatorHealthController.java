package com.loglens.aggregator.controller;

import com.loglens.aggregator.consumer.LogAggregatorConsumer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/aggregator")
@RequiredArgsConstructor
@Tag(name = "Aggregator Health", description = "Aggregator service health and metrics")
public class AggregatorHealthController {

    private final LogAggregatorConsumer consumer;

    @GetMapping("/health")
    @Operation(summary = "Get aggregator health and processing statistics")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "logsProcessed", consumer.getLogsProcessed(),
                "alertsFired", consumer.getAlertsFired()
        ));
    }
}
