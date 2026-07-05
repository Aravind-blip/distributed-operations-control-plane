package com.opscontrolplane.emsbridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opscontrolplane.emsbridge.dto.EmsMessage;
import com.opscontrolplane.kafka.KafkaTopics;
import com.opscontrolplane.kafka.event.ServiceHealthEvent;
import com.opscontrolplane.kafka.event.WorkflowEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class EmsBridgeService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public EmsBridgeService(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Routes based on the mock JMS destination name: health-oriented destinations become
     * ServiceHealthEvents, everything else is treated as a workflow-triggering message.
     * This mirrors how a real EMS bridge would route by queue/topic name rather than payload shape.
     */
    public Map<String, Object> normalizeAndPublish(EmsMessage message) {
        String destination = message.destination().toLowerCase();
        Instant occurredAt = message.timestamp() != null ? message.timestamp() : Instant.now();

        if (destination.contains("health") || destination.contains("telemetry")) {
            Map<String, Object> payload = objectMapper.convertValue(message.payload(),
                    objectMapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, Object.class));

            String serviceName = String.valueOf(payload.getOrDefault("serviceName", "Unknown Service"));
            String status = String.valueOf(payload.getOrDefault("status", "HEALTHY"));
            double latencyMs = toDouble(payload.get("latencyMs"));
            double errorRate = toDouble(payload.get("errorRate"));

            ServiceHealthEvent event = new ServiceHealthEvent(serviceName, status, latencyMs, errorRate,
                    occurredAt, "ems-bridge");
            kafkaTemplate.send(KafkaTopics.SERVICE_HEALTH_EVENTS, serviceName, event);
            return Map.of("routedTo", KafkaTopics.SERVICE_HEALTH_EVENTS, "event", event, "messageId", message.messageId());
        }

        WorkflowEvent event = new WorkflowEvent(UUID.randomUUID(), "EMS_TRIGGERED_WORKFLOW", "REQUESTED",
                "ems-bridge", occurredAt);
        kafkaTemplate.send(KafkaTopics.WORKFLOW_EVENTS, event.workflowId().toString(), event);
        return Map.of("routedTo", KafkaTopics.WORKFLOW_EVENTS, "event", event, "messageId", message.messageId());
    }

    private static double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(value.toString());
    }
}
