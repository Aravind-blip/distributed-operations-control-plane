package com.opscontrolplane.services.dto;

import com.opscontrolplane.services.Environment;
import com.opscontrolplane.services.ServiceStatus;

public record UpdateDistributedServiceRequest(
        String name,
        Environment environment,
        String region,
        ServiceStatus status,
        Double latencyMs,
        Double errorRate,
        String version,
        String ownerTeam,
        String serviceType
) {
}
