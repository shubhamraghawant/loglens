package com.loglens.query.controller;

import com.loglens.query.dto.ServiceStats;
import com.loglens.query.entity.LogEntryEntity;
import com.loglens.query.repository.LogEntryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
@Tag(name = "Log Query", description = "Query and search log entries")
public class LogQueryController {

    private final LogEntryRepository logEntryRepository;

    @GetMapping
    @Operation(summary = "Query logs with optional filters")
    public ResponseEntity<Page<LogEntryEntity>> getLogs(
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        if (serviceName != null && level != null) {
            return ResponseEntity.ok(logEntryRepository.findByServiceNameAndLevel(serviceName, level.toUpperCase(), pageable));
        } else if (serviceName != null) {
            return ResponseEntity.ok(logEntryRepository.findByServiceName(serviceName, pageable));
        } else if (level != null) {
            return ResponseEntity.ok(logEntryRepository.findByLevel(level.toUpperCase(), pageable));
        }
        return ResponseEntity.ok(logEntryRepository.findAll(pageable));
    }

    @GetMapping("/services")
    @Operation(summary = "Get list of distinct service names")
    public ResponseEntity<List<String>> getServices() {
        return ResponseEntity.ok(logEntryRepository.findDistinctServiceNames());
    }

    @GetMapping("/stats")
    @Operation(summary = "Get log counts by service and level for last 24 hours")
    public ResponseEntity<List<ServiceStats>> getStats() {
        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
        List<Object[]> raw = logEntryRepository.countByServiceAndLevelSince(since);

        Map<String, ServiceStats> statsMap = new HashMap<>();
        for (Object[] row : raw) {
            String svc = (String) row[0];
            String lvl = (String) row[1];
            long cnt = (Long) row[2];
            statsMap.computeIfAbsent(svc, k -> new ServiceStats(k, 0, 0, 0, 0));
            ServiceStats s = statsMap.get(svc);
            switch (lvl) {
                case "INFO" -> s.setInfoCount(cnt);
                case "WARN" -> s.setWarnCount(cnt);
                case "ERROR" -> s.setErrorCount(cnt);
                case "FATAL" -> s.setFatalCount(cnt);
            }
        }
        return ResponseEntity.ok(new ArrayList<>(statsMap.values()));
    }
}
