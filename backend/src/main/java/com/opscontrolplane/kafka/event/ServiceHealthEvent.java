package com.opscontrolplane.kafka.event;

import java.time.Instant;

/**
 * Event payload published to {@code service-health-events}. serviceName is used
 * (rather than an id) so producers that don't hold a DB-generated UUID -- e.g. the
 * SOAP adapter and EMS bridge simulators -- can still address a known seeded service.
 */
public record ServiceHealthEvent(
        String serviceName,
        String status,
        double latencyMs,
        double errorRate,
        Instant observedAt,
        String source
) {
}
