package com.opscontrolplane.alerts.dto;

import com.opscontrolplane.alerts.Alert;
import com.opscontrolplane.alerts.AlertSeverity;
import com.opscontrolplane.alerts.AlertStatus;

import java.time.Instant;
import java.util.UUID;

public record AlertResponse(
        UUID id,
        AlertSeverity severity,
        UUID sourceServiceId,
        String title,
        String description,
        AlertStatus status,
        Instant createdAt,
        String acknowledgedBy,
        String resolvedBy,
        Instant resolvedAt
) {

    public static AlertResponse from(Alert alert) {
        return new AlertResponse(alert.getId(), alert.getSeverity(), alert.getSourceServiceId(), alert.getTitle(),
                alert.getDescription(), alert.getStatus(), alert.getCreatedAt(), alert.getAcknowledgedBy(),
                alert.getResolvedBy(), alert.getResolvedAt());
    }
}
