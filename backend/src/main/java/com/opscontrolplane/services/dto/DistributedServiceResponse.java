package com.opscontrolplane.services.dto;

import com.opscontrolplane.services.DistributedService;
import com.opscontrolplane.services.Environment;
import com.opscontrolplane.services.ServiceStatus;

import java.time.Instant;
import java.util.UUID;

public record DistributedServiceResponse(
        UUID id,
        String name,
        Environment environment,
        String region,
        ServiceStatus status,
        double latencyMs,
        double errorRate,
        Instant lastHeartbeat,
        String version,
        String ownerTeam,
        String serviceType,
        Instant createdAt,
        Instant updatedAt
) {

    public static DistributedServiceResponse from(DistributedService s) {
        return new DistributedServiceResponse(
                s.getId(), s.getName(), s.getEnvironment(), s.getRegion(), s.getStatus(),
                s.getLatencyMs(), s.getErrorRate(), s.getLastHeartbeat(), s.getVersion(),
                s.getOwnerTeam(), s.getServiceType(), s.getCreatedAt(), s.getUpdatedAt());
    }
}
