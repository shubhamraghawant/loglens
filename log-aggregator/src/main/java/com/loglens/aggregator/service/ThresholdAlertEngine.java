package com.loglens.aggregator.service;

import com.loglens.aggregator.entity.AlertEntity;
import com.loglens.common.enums.LogLevel;
import com.loglens.common.model.LogEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ThresholdAlertEngine {

    // Key format: serviceName::level
    private final ConcurrentHashMap<String, Deque<Instant>> eventWindows = new ConcurrentHashMap<>();

    record AlertRule(LogLevel level, int threshold, int windowSeconds, String severity) {}

    private static final List<AlertRule> RULES = List.of(
            new AlertRule(LogLevel.ERROR, 25, 60, "CRITICAL"),
            new AlertRule(LogLevel.ERROR, 10, 60, "WARN"),
            new AlertRule(LogLevel.FATAL, 3, 60, "CRITICAL")
    );

    public Optional<AlertEntity> evaluate(LogEntry entry) {
        if (entry.level() == LogLevel.INFO || entry.level() == LogLevel.WARN) {
            return Optional.empty();
        }

        String key = entry.serviceName() + "::" + entry.level().name();
        Instant now = Instant.now();

        eventWindows.compute(key, (k, deque) -> {
            if (deque == null) deque = new ArrayDeque<>();
            deque.addLast(now);
            return deque;
        });

        Deque<Instant> window = eventWindows.get(key);

        // Find the most severe triggered alert for this event
        AlertRule bestMatch = null;
        int matchCount = 0;

        for (AlertRule rule : RULES) {
            if (rule.level() != entry.level()) continue;

            Instant cutoff = now.minus(rule.windowSeconds(), ChronoUnit.SECONDS);
            long count = window.stream().filter(t -> t.isAfter(cutoff)).count();

            if (count >= rule.threshold()) {
                if (bestMatch == null || severityScore(rule.severity()) > severityScore(bestMatch.severity())) {
                    bestMatch = rule;
                    matchCount = (int) count;
                }
            }
        }

        if (bestMatch == null) return Optional.empty();

        // Prune old events from deque (keep clean)
        int finalWindowSec = bestMatch.windowSeconds();
        Instant finalCutoff = now.minus(finalWindowSec, ChronoUnit.SECONDS);
        eventWindows.compute(key, (k, deque) -> {
            if (deque != null) {
                while (!deque.isEmpty() && deque.peekFirst().isBefore(finalCutoff)) {
                    deque.pollFirst();
                }
            }
            return deque;
        });

        AlertRule rule = bestMatch;
        int count = matchCount;

        AlertEntity alert = AlertEntity.builder()
                .id(UUID.randomUUID().toString())
                .serviceName(entry.serviceName())
                .level(entry.level().name())
                .count(count)
                .threshold(rule.threshold())
                .message(String.format("[%s] %s: %d occurrences of %s in %ds (threshold: %d)",
                        rule.severity(), entry.serviceName(), count, entry.level().name(),
                        rule.windowSeconds(), rule.threshold()))
                .severity(rule.severity())
                .createdAt(now)
                .acknowledged(false)
                .build();

        log.warn("Alert triggered: {} - {} {} events in {}s", rule.severity(),
                entry.serviceName(), count, rule.windowSeconds());

        return Optional.of(alert);
    }

    public void reset() {
        eventWindows.clear();
    }

    public int getWindowSize(String serviceName, LogLevel level) {
        String key = serviceName + "::" + level.name();
        Deque<Instant> deque = eventWindows.get(key);
        return deque == null ? 0 : deque.size();
    }

    private int severityScore(String severity) {
        return "CRITICAL".equals(severity) ? 2 : 1;
    }
}
