package com.loglens.query.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceStats {
    private String serviceName;
    private long infoCount;
    private long warnCount;
    private long errorCount;
    private long fatalCount;
}
