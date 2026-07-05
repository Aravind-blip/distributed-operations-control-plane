package com.opscontrolplane.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opscontrolplane.kafka.KafkaTopics;
import com.opscontrolplane.kafka.event.AuditEvent;
import com.opscontrolplane.kafka.event.WorkflowEvent;
import com.opscontrolplane.metrics.OpsMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class WorkflowEventAuditConsumer {

    private static final Logger log = LoggerFactory.getLogger(WorkflowEventAuditConsumer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final OpsMetrics metrics;

    public WorkflowEventAuditConsumer(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper,
                                       OpsMetrics metrics) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.metrics = metrics;
    }

    @KafkaListener(topics = KafkaTopics.WORKFLOW_EVENTS, groupId = "ops-control-plane-workflow-audit")
    public void onWorkflowEvent(WorkflowEvent event) {
        try {
            String metadata = objectMapper.writeValueAsString(event);
            AuditEvent auditEvent = new AuditEvent(
                    event.actor() != null ? event.actor() : "kafka-consumer",
                    "WORKFLOW_" + event.status(), "Workflow", event.workflowId().toString(),
                    metadata, Instant.now());
            kafkaTemplate.send(KafkaTopics.AUDIT_EVENTS, event.workflowId().toString(), auditEvent);
            metrics.kafkaMessagesProcessed().increment();
        } catch (Exception ex) {
            metrics.kafkaMessagesFailed().increment();
            log.error("Failed to bridge workflow event {} to audit-events", event.workflowId(), ex);
        }
    }
}
