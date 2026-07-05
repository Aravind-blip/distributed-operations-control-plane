package com.opscontrolplane.kafka.event;

import java.time.Instant;

public record AuditEvent(
        String actor,
        String action,
        String resourceType,
        String resourceId,
        String metadata,
        Instant occurredAt
) {
}
