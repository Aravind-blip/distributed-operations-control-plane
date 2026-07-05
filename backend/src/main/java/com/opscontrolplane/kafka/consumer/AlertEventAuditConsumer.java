package com.opscontrolplane.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opscontrolplane.kafka.KafkaTopics;
import com.opscontrolplane.kafka.event.AlertEvent;
import com.opscontrolplane.kafka.event.AuditEvent;
import com.opscontrolplane.metrics.OpsMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Bridges downstream domain events into the audit trail topic, so every alert
 * lifecycle transition -- including ones created automatically by Kafka consumers
 * rather than a human via the REST API -- ends up recorded in the audit log.
 */
@Component
public class AlertEventAuditConsumer {

    private static final Logger log = LoggerFactory.getLogger(AlertEventAuditConsumer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final OpsMetrics metrics;

    public AlertEventAuditConsumer(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper,
                                    OpsMetrics metrics) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.metrics = metrics;
    }

    @KafkaListener(topics = KafkaTopics.ALERT_EVENTS, groupId = "ops-control-plane-alert-audit")
    public void onAlertEvent(AlertEvent event) {
        try {
            String metadata = objectMapper.writeValueAsString(event);
            AuditEvent auditEvent = new AuditEvent(
                    "kafka-consumer", "ALERT_" + event.status(), "Alert", event.alertId().toString(),
                    metadata, Instant.now());
            kafkaTemplate.send(KafkaTopics.AUDIT_EVENTS, event.alertId().toString(), auditEvent);
            metrics.kafkaMessagesProcessed().increment();
        } catch (Exception ex) {
            metrics.kafkaMessagesFailed().increment();
            log.error("Failed to bridge alert event {} to audit-events", event.alertId(), ex);
        }
    }
}
