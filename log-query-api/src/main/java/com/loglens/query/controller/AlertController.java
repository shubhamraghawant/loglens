package com.loglens.query.controller;

import com.loglens.query.entity.AlertEntity;
import com.loglens.query.repository.AlertRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Alert management API")
public class AlertController {

    private final AlertRepository alertRepository;

    @GetMapping
    @Operation(summary = "Get all alerts")
    public ResponseEntity<List<AlertEntity>> getAlerts(
            @RequestParam(required = false) Boolean acknowledged,
            @RequestParam(required = false) String severity) {
        if (severity != null) {
            return ResponseEntity.ok(alertRepository.findBySeverityOrderByCreatedAtDesc(severity.toUpperCase()));
        }
        if (Boolean.FALSE.equals(acknowledged)) {
            return ResponseEntity.ok(alertRepository.findByAcknowledgedFalseOrderByCreatedAtDesc());
        }
        return ResponseEntity.ok(alertRepository.findAll());
    }

    @GetMapping("/active")
    @Operation(summary = "Get active (unacknowledged) critical alerts")
    public ResponseEntity<List<AlertEntity>> getActiveAlerts() {
        return ResponseEntity.ok(alertRepository.findByAcknowledgedFalseAndSeverityOrderByCreatedAtDesc("CRITICAL"));
    }

    @PatchMapping("/{id}/acknowledge")
    @Operation(summary = "Acknowledge an alert")
    public ResponseEntity<AlertEntity> acknowledgeAlert(@PathVariable UUID id) {
        return alertRepository.findById(id)
                .map(alert -> {
                    alert.setAcknowledged(true);
                    return ResponseEntity.ok(alertRepository.save(alert));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
