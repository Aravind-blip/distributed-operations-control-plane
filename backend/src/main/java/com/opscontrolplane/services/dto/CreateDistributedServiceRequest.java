package com.opscontrolplane.services.dto;

import com.opscontrolplane.services.Environment;
import com.opscontrolplane.services.ServiceStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDistributedServiceRequest(
        @NotBlank String name,
        @NotNull Environment environment,
        @NotBlank String region,
        @NotNull ServiceStatus status,
        @DecimalMin(value = "0.0") double latencyMs,
        @DecimalMin(value = "0.0") double errorRate,
        @NotBlank String version,
        @NotBlank String ownerTeam,
        @NotBlank String serviceType
) {
}
