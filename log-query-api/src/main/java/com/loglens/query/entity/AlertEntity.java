package com.loglens.query.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alerts")
@Data
public class AlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String serviceName;
    private String level;
    private int count;
    private int threshold;
    private String message;
    private String severity;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean acknowledged;
}
