package com.loglens.aggregator.consumer;

import com.loglens.aggregator.entity.AlertEntity;
import com.loglens.aggregator.entity.LogEntryEntity;
import com.loglens.aggregator.repository.AlertRepository;
import com.loglens.aggregator.repository.LogEntryRepository;
import com.loglens.aggregator.service.ThresholdAlertEngine;
import com.loglens.common.model.AlertEvent;
import com.loglens.common.model.LogEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogAggregatorConsumer {

    private final LogEntryRepository logEntryRepository;
    private final AlertRepository alertRepository;
    private final ThresholdAlertEngine alertEngine;
    private final KafkaTemplate<String, AlertEvent> alertKafkaTemplate;

    private final AtomicLong logsProcessed = new AtomicLong(0);
    private final AtomicLong alertsFired = new AtomicLong(0);

    @KafkaListener(topics = "logs.raw", groupId = "loglens-aggregator",
                   containerFactory = "kafkaListenerContainerFactory")
    public void consume(LogEntry logEntry) {
        try {
            // Persist log entry
            LogEntryEntity entity = LogEntryEntity.builder()
                    .id(logEntry.id())
                    .serviceName(logEntry.serviceName())
                    .level(logEntry.level().name())
                    .message(logEntry.message())
                    .timestamp(logEntry.timestamp())
                    .traceId(logEntry.traceId())
                    .createdAt(Instant.now())
                    .build();
            logEntryRepository.save(entity);
            logsProcessed.incrementAndGet();

            log.debug("Saved log [{}] from {} - {}", logEntry.level(), logEntry.serviceName(), logEntry.id());

            // Evaluate thresholds
            Optional<AlertEntity> maybeAlert = alertEngine.evaluate(logEntry);
            maybeAlert.ifPresent(alert -> {
                alertRepository.save(alert);
                alertsFired.incrementAndGet();

                AlertEvent event = new AlertEvent(
                        alert.getId(),
                        alert.getServiceName(),
                        logEntry.level(),
                        alert.getCount(),
                        alert.getThreshold(),
                        60,
                        alert.getCreatedAt(),
                        alert.getSeverity()
                );
                alertKafkaTemplate.send("logs.alerts", alert.getId(), event);
                log.warn("Alert fired and published: [{}] {} - {} events", alert.getSeverity(),
                        alert.getServiceName(), alert.getCount());
            });
        } catch (Exception e) {
            log.error("Error processing log entry {}: {}", logEntry.id(), e.getMessage(), e);
        }
    }

    public long getLogsProcessed() {
        return logsProcessed.get();
    }

    public long getAlertsFired() {
        return alertsFired.get();
    }
}
