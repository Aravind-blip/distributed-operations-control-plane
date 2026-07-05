package com.opscontrolplane.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record WorkflowEvent(
        UUID workflowId,
        String workflowType,
        String status,
        String actor,
        Instant occurredAt
) {
}
