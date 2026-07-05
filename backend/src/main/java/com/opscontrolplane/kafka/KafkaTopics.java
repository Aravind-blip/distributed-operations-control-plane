package com.opscontrolplane.kafka;

public final class KafkaTopics {

    public static final String SERVICE_HEALTH_EVENTS = "service-health-events";
    public static final String ALERT_EVENTS = "alert-events";
    public static final String WORKFLOW_EVENTS = "workflow-events";
    public static final String AUDIT_EVENTS = "audit-events";

    private KafkaTopics() {
    }
}
