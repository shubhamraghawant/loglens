package com.loglens.query.controller;

import com.loglens.query.entity.LogEntryEntity;
import com.loglens.query.repository.LogEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LogQueryController.class)
class LogQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LogEntryRepository logEntryRepository;

    @Test
    void shouldReturnLogs() throws Exception {
        LogEntryEntity e = new LogEntryEntity();
        e.setId("id-1");
        e.setServiceName("order-service");
        e.setLevel("ERROR");
        e.setMessage("Something went wrong");
        e.setTimestamp(Instant.now());
        e.setCreatedAt(Instant.now());

        when(logEntryRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(e)));

        mockMvc.perform(get("/api/v1/logs"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnServices() throws Exception {
        when(logEntryRepository.findDistinctServiceNames()).thenReturn(List.of("order-service", "payment-service"));
        mockMvc.perform(get("/api/v1/logs/services")).andExpect(status().isOk());
    }
}
