package com.loglens.producer.dto;

import com.loglens.common.enums.LogLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EmitLogRequest(
        @NotBlank(message = "serviceName is required")
        String serviceName,

        @NotNull(message = "level is required")
        LogLevel level,

        @NotBlank(message = "message is required")
        String message
) {}
