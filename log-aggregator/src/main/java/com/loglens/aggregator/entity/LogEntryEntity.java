package com.loglens.aggregator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "log_entries", indexes = {
        @Index(name = "idx_log_service", columnList = "service_name"),
        @Index(name = "idx_log_level", columnList = "level"),
        @Index(name = "idx_log_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogEntryEntity {

    @Id
    @Column(nullable = false, length = 36)
    private String id;

    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(nullable = false, length = 10)
    private String level;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(name = "trace_id", length = 32)
    private String traceId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
