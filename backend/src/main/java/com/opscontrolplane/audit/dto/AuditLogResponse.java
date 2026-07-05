package com.opscontrolplane.audit.dto;

import com.opscontrolplane.audit.AuditLog;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        String actor,
        String action,
        String resourceType,
        String resourceId,
        String metadata,
        Instant createdAt
) {

    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getActor(), log.getAction(), log.getResourceType(),
                log.getResourceId(), log.getMetadata(), log.getCreatedAt());
    }
}
