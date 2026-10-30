package com.loglens.producer.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class SimulationState {

    private final AtomicBoolean running = new AtomicBoolean(false);

    public boolean isRunning() {
        return running.get();
    }

    public boolean start() {
        return running.compareAndSet(false, true);
    }

    public boolean stop() {
        return running.compareAndSet(true, false);
    }
}
