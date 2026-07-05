package com.opscontrolplane.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Holds Micrometer Counter/Timer beans so services can inject a single instrumentation
 * facade instead of re-declaring meters ad hoc across modules.
 */
@Component
public class OpsMetrics {

    private final Counter alertsCreated;
    private final Counter alertsAcknowledged;
    private final Counter alertsResolved;
    private final Counter kafkaMessagesProcessed;
    private final Counter kafkaMessagesFailed;
    private final Counter workflowsApproved;
    private final Counter workflowsRejected;

    private final Timer alertAcknowledgeTimer;
    private final Timer alertResolveTimer;
    private final Timer workflowApproveTimer;
    private final Timer workflowRejectTimer;

    public OpsMetrics(MeterRegistry registry) {
        this.alertsCreated = Counter.builder("alerts.created")
                .description("Number of alerts created")
                .register(registry);
        this.alertsAcknowledged = Counter.builder("alerts.acknowledged")
                .description("Number of alerts acknowledged")
                .register(registry);
        this.alertsResolved = Counter.builder("alerts.resolved")
                .description("Number of alerts resolved")
                .register(registry);
        this.kafkaMessagesProcessed = Counter.builder("kafka.messages.processed")
                .description("Number of Kafka messages successfully processed")
                .register(registry);
        this.kafkaMessagesFailed = Counter.builder("kafka.messages.failed")
                .description("Number of Kafka messages that failed processing")
                .register(registry);
        this.workflowsApproved = Counter.builder("workflows.approved")
                .description("Number of workflows approved")
                .register(registry);
        this.workflowsRejected = Counter.builder("workflows.rejected")
                .description("Number of workflows rejected")
                .register(registry);

        this.alertAcknowledgeTimer = Timer.builder("alerts.acknowledge.duration")
                .description("Time taken to acknowledge an alert")
                .register(registry);
        this.alertResolveTimer = Timer.builder("alerts.resolve.duration")
                .description("Time taken to resolve an alert")
                .register(registry);
        this.workflowApproveTimer = Timer.builder("workflows.approve.duration")
                .description("Time taken to approve a workflow")
                .register(registry);
        this.workflowRejectTimer = Timer.builder("workflows.reject.duration")
                .description("Time taken to reject a workflow")
                .register(registry);
    }

    public Counter alertsCreated() {
        return alertsCreated;
    }

    public Counter alertsAcknowledged() {
        return alertsAcknowledged;
    }

    public Counter alertsResolved() {
        return alertsResolved;
    }

    public Counter kafkaMessagesProcessed() {
        return kafkaMessagesProcessed;
    }

    public Counter kafkaMessagesFailed() {
        return kafkaMessagesFailed;
    }

    public Counter workflowsApproved() {
        return workflowsApproved;
    }

    public Counter workflowsRejected() {
        return workflowsRejected;
    }

    public Timer alertAcknowledgeTimer() {
        return alertAcknowledgeTimer;
    }

    public Timer alertResolveTimer() {
        return alertResolveTimer;
    }

    public Timer workflowApproveTimer() {
        return workflowApproveTimer;
    }

    public Timer workflowRejectTimer() {
        return workflowRejectTimer;
    }
}
