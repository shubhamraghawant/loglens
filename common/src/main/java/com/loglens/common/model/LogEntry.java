package com.loglens.common.model;

import com.loglens.common.enums.LogLevel;
import java.time.Instant;

public record LogEntry(
        String id,
        String serviceName,
        LogLevel level,
        String message,
        Instant timestamp,
        String traceId
) {}
