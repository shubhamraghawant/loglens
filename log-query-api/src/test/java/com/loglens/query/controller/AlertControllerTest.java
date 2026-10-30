package com.loglens.query.controller;

import com.loglens.query.repository.AlertRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlertController.class)
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlertRepository alertRepository;

    @Test
    void shouldReturnAlerts() throws Exception {
        when(alertRepository.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/alerts")).andExpect(status().isOk());
    }

    @Test
    void shouldReturnActiveAlerts() throws Exception {
        when(alertRepository.findByAcknowledgedFalseAndSeverityOrderByCreatedAtDesc("CRITICAL")).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/alerts/active")).andExpect(status().isOk());
    }
}
