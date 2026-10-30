package com.loglens.query.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "log_entries")
@Data
public class LogEntryEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String serviceName;

    @Column(nullable = false)
    private String level;

    @Column(columnDefinition = "TEXT")
    private String message;

    private Instant timestamp;
    private String traceId;

    @Column(nullable = false)
    private Instant createdAt;
}
