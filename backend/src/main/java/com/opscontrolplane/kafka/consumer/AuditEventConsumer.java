package com.opscontrolplane.kafka.consumer;

import com.opscontrolplane.audit.AuditService;
import com.opscontrolplane.kafka.KafkaTopics;
import com.opscontrolplane.kafka.event.AuditEvent;
import com.opscontrolplane.metrics.OpsMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Terminal consumer for the audit-events topic: persists every event that other
 * consumers bridged from alert/workflow lifecycle changes into the AuditLog table,
 * giving a durable, queryable trail independent of the originating REST call.
 */
@Component
public class AuditEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditEventConsumer.class);

    private final AuditService auditService;
    private final OpsMetrics metrics;

    public AuditEventConsumer(AuditService auditService, OpsMetrics metrics) {
        this.auditService = auditService;
        this.metrics = metrics;
    }

    @KafkaListener(topics = KafkaTopics.AUDIT_EVENTS, groupId = "ops-control-plane-audit-persist")
    public void onAuditEvent(AuditEvent event) {
        try {
            auditService.record(event.actor(), event.action(), event.resourceType(), event.resourceId(), event.metadata());
            metrics.kafkaMessagesProcessed().increment();
        } catch (Exception ex) {
            metrics.kafkaMessagesFailed().increment();
            log.error("Failed to persist audit event for resource {}", event.resourceId(), ex);
        }
    }
}
