package com.loglens.producer.service;

import com.loglens.common.enums.LogLevel;
import com.loglens.common.model.LogEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogSimulatorService {

    private static final String TOPIC = "logs.raw";
    private final KafkaTemplate<String, LogEntry> kafkaTemplate;
    private final SimulationState simulationState;
    private final Random random = new Random();

    private static final List<String> SERVICES = List.of(
            "order-service",
            "payment-service",
            "user-service",
            "inventory-service",
            "notification-service"
    );

    private static final Map<String, List<String>> SERVICE_MESSAGES = Map.of(
            "order-service", List.of(
                    "Processing order #%d for customer %d",
                    "Order #%d created successfully",
                    "Order #%d payment confirmed",
                    "Failed to process order #%d: insufficient stock",
                    "Order #%d dispatched to warehouse",
                    "Order cancellation requested for order #%d",
                    "Order #%d delivery scheduled"
            ),
            "payment-service", List.of(
                    "Payment gateway timeout for transaction %d",
                    "Payment of $%.2f processed successfully for order #%d",
                    "Refund initiated for order #%d",
                    "Payment declined: insufficient funds for customer %d",
                    "Fraud detection triggered for transaction %d",
                    "Payment method validation failed for customer %d",
                    "Stripe webhook received for charge %d"
            ),
            "user-service", List.of(
                    "User login successful for userId=%d",
                    "User registration completed for email user%d@example.com",
                    "Password reset requested for userId=%d",
                    "Session expired for userId=%d",
                    "User profile updated for userId=%d",
                    "Failed login attempt %d for userId=%d",
                    "Account locked after multiple failed attempts for userId=%d"
            ),
            "inventory-service", List.of(
                    "Stock level updated for productId=%d: %d units remaining",
                    "Low stock alert for productId=%d: only %d units left",
                    "Restock request created for productId=%d",
                    "Inventory sync completed: %d products updated",
                    "Product %d marked as out of stock",
                    "Warehouse transfer initiated for %d units of productId=%d",
                    "SKU mismatch detected for productId=%d"
            ),
            "notification-service", List.of(
                    "Email sent to user%d@example.com",
                    "SMS notification delivered to +1-555-%07d",
                    "Push notification sent to %d devices",
                    "Email delivery failed for user%d@example.com",
                    "Notification queue depth: %d messages",
                    "Template rendering failed for templateId=%d",
                    "Notification retry %d for messageId=%d"
            )
    );

    @Scheduled(fixedDelay = 2000)
    public void emitSimulatedLog() {
        if (!simulationState.isRunning()) {
            return;
        }
        String serviceName = SERVICES.get(random.nextInt(SERVICES.size()));
        LogLevel level = weightedRandomLevel();
        String message = generateMessage(serviceName, level);
        LogEntry entry = new LogEntry(
                UUID.randomUUID().toString(),
                serviceName,
                level,
                message,
                Instant.now(),
                UUID.randomUUID().toString().replace("-", "").substring(0, 16)
        );
        publishLog(entry);
    }

    public void publishLog(LogEntry entry) {
        kafkaTemplate.send(TOPIC, entry.id(), entry)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish log entry {}: {}", entry.id(), ex.getMessage());
                    } else {
                        log.debug("Published log [{}] {} - {}", entry.level(), entry.serviceName(), entry.message());
                    }
                });
    }

    private LogLevel weightedRandomLevel() {
        int roll = random.nextInt(100);
        if (roll < 60) return LogLevel.INFO;
        else if (roll < 85) return LogLevel.WARN;
        else if (roll < 98) return LogLevel.ERROR;
        else return LogLevel.FATAL;
    }

    private String generateMessage(String serviceName, LogLevel level) {
        List<String> templates = SERVICE_MESSAGES.getOrDefault(serviceName, List.of("Log event from %s"));
        String template = templates.get(random.nextInt(templates.size()));
        // Fill in format parameters with random numbers
        int count = countFormatSpecifiers(template);
        Object[] args = new Object[count];
        for (int i = 0; i < count; i++) {
            args[i] = random.nextInt(100000);
        }
        try {
            return String.format(template, args);
        } catch (Exception e) {
            return template;
        }
    }

    private int countFormatSpecifiers(String template) {
        int count = 0;
        boolean inFormat = false;
        for (int i = 0; i < template.length(); i++) {
            if (template.charAt(i) == '%' && i + 1 < template.length()) {
                char next = template.charAt(i + 1);
                if (next == 'd' || next == 's' || next == 'f') {
                    count++;
                    i++;
                } else if (Character.isDigit(next) || next == '.') {
                    count++;
                }
            }
        }
        return count;
    }
}
