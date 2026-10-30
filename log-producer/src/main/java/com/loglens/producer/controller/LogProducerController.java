package com.loglens.producer.controller;

import com.loglens.common.model.LogEntry;
import com.loglens.producer.dto.EmitLogRequest;
import com.loglens.producer.service.LogSimulatorService;
import com.loglens.producer.service.SimulationState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
@Tag(name = "Log Producer", description = "APIs for emitting logs and controlling simulation")
public class LogProducerController {

    private final LogSimulatorService simulatorService;
    private final SimulationState simulationState;

    @PostMapping("/emit")
    @Operation(summary = "Emit a single log entry to Kafka")
    public ResponseEntity<Map<String, String>> emitLog(@Valid @RequestBody EmitLogRequest request) {
        LogEntry entry = new LogEntry(
                UUID.randomUUID().toString(),
                request.serviceName(),
                request.level(),
                request.message(),
                Instant.now(),
                UUID.randomUUID().toString().replace("-", "").substring(0, 16)
        );
        simulatorService.publishLog(entry);
        return ResponseEntity.ok(Map.of(
                "status", "emitted",
                "id", entry.id(),
                "traceId", entry.traceId()
        ));
    }

    @PostMapping("/simulate/start")
    @Operation(summary = "Start the log simulation")
    public ResponseEntity<Map<String, String>> startSimulation() {
        boolean started = simulationState.start();
        String message = started ? "Simulation started successfully" : "Simulation is already running";
        return ResponseEntity.ok(Map.of("status", started ? "started" : "already_running", "message", message));
    }

    @PostMapping("/simulate/stop")
    @Operation(summary = "Stop the log simulation")
    public ResponseEntity<Map<String, String>> stopSimulation() {
        boolean stopped = simulationState.stop();
        String message = stopped ? "Simulation stopped successfully" : "Simulation was not running";
        return ResponseEntity.ok(Map.of("status", stopped ? "stopped" : "not_running", "message", message));
    }

    @GetMapping("/simulate/status")
    @Operation(summary = "Get simulation status")
    public ResponseEntity<Map<String, Boolean>> getSimulationStatus() {
        return ResponseEntity.ok(Map.of("running", simulationState.isRunning()));
    }
}
