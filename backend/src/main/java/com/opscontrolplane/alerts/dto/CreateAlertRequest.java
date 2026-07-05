package com.opscontrolplane.alerts.dto;

import com.opscontrolplane.alerts.AlertSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAlertRequest(
        @NotNull AlertSeverity severity,
        @NotNull UUID sourceServiceId,
        @NotBlank String title,
        @NotBlank String description
) {
}
