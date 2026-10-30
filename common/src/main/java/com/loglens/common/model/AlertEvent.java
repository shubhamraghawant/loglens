package com.loglens.common.model;

import com.loglens.common.enums.LogLevel;
import java.time.Instant;

public record AlertEvent(
        String id,
        String serviceName,
        LogLevel level,
        int count,
        int threshold,
        int windowSeconds,
        Instant triggeredAt,
        String severity
) {}
