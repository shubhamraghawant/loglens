package com.loglens.aggregator.service;

import com.loglens.common.enums.LogLevel;
import com.loglens.common.model.LogEntry;
import com.loglens.aggregator.entity.AlertEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ThresholdAlertEngineTest {

    private ThresholdAlertEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ThresholdAlertEngine();
    }

    private LogEntry buildEntry(String service, LogLevel level) {
        return new LogEntry(UUID.randomUUID().toString(), service, level, "Test message", Instant.now(), UUID.randomUUID().toString());
    }

    @Test
    void shouldNotAlertBelowThreshold() {
        for (int i = 0; i < 5; i++) {
            Optional<AlertEntity> alert = engine.evaluate(buildEntry("order-service", LogLevel.ERROR));
            assertThat(alert).isEmpty();
        }
    }

    @Test
    void shouldFireWarnAlertOnTenErrors() {
        Optional<AlertEntity> alert = Optional.empty();
        for (int i = 0; i < 11; i++) {
            alert = engine.evaluate(buildEntry("payment-service", LogLevel.ERROR));
        }
        assertThat(alert).isPresent();
        assertThat(alert.get().getSeverity()).isEqualTo("WARN");
    }

    @Test
    void shouldFireCriticalAlertOnFourFatals() {
        Optional<AlertEntity> alert = Optional.empty();
        for (int i = 0; i < 4; i++) {
            alert = engine.evaluate(buildEntry("user-service", LogLevel.FATAL));
        }
        assertThat(alert).isPresent();
        assertThat(alert.get().getSeverity()).isEqualTo("CRITICAL");
    }
}
