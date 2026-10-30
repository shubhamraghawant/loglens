package com.loglens.producer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loglens.common.enums.LogLevel;
import com.loglens.producer.dto.EmitLogRequest;
import com.loglens.producer.service.LogSimulatorService;
import com.loglens.producer.service.SimulationState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LogProducerController.class)
class LogProducerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LogSimulatorService simulatorService;

    @MockBean
    private SimulationState simulationState;

    @Test
    void emitLog_validRequest_returns200() throws Exception {
        EmitLogRequest request = new EmitLogRequest("order-service", LogLevel.ERROR, "Test error message");
        doNothing().when(simulatorService).publishLog(any());

        mockMvc.perform(post("/api/v1/logs/emit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("emitted"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.traceId").exists());

        verify(simulatorService, times(1)).publishLog(any());
    }

    @Test
    void emitLog_missingServiceName_returns400() throws Exception {
        String badRequest = """
                {"level": "ERROR", "message": "Test error"}
                """;

        mockMvc.perform(post("/api/v1/logs/emit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void emitLog_missingLevel_returns400() throws Exception {
        String badRequest = """
                {"serviceName": "order-service", "message": "Test error"}
                """;

        mockMvc.perform(post("/api/v1/logs/emit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void startSimulation_whenNotRunning_returnsStarted() throws Exception {
        when(simulationState.start()).thenReturn(true);

        mockMvc.perform(post("/api/v1/logs/simulate/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("started"));
    }

    @Test
    void startSimulation_whenAlreadyRunning_returnsAlreadyRunning() throws Exception {
        when(simulationState.start()).thenReturn(false);

        mockMvc.perform(post("/api/v1/logs/simulate/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("already_running"));
    }

    @Test
    void stopSimulation_whenRunning_returnsStopped() throws Exception {
        when(simulationState.stop()).thenReturn(true);

        mockMvc.perform(post("/api/v1/logs/simulate/stop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("stopped"));
    }

    @Test
    void stopSimulation_whenNotRunning_returnsNotRunning() throws Exception {
        when(simulationState.stop()).thenReturn(false);

        mockMvc.perform(post("/api/v1/logs/simulate/stop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("not_running"));
    }

    @Test
    void getSimulationStatus_returnsCurrentState() throws Exception {
        when(simulationState.isRunning()).thenReturn(true);

        mockMvc.perform(get("/api/v1/logs/simulate/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running").value(true));
    }

    @Test
    void getSimulationStatus_whenStopped_returnsFalse() throws Exception {
        when(simulationState.isRunning()).thenReturn(false);

        mockMvc.perform(get("/api/v1/logs/simulate/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running").value(false));
    }
}
