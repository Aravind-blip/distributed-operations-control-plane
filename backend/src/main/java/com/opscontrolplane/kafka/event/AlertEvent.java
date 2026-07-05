package com.opscontrolplane.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record AlertEvent(
        UUID alertId,
        UUID sourceServiceId,
        String severity,
        String status,
        String title,
        Instant occurredAt
) {
}
