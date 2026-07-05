package com.opscontrolplane.metrics.dto;

public record DashboardSummaryResponse(
        long totalServices,
        long healthyServices,
        long degradedServices,
        long offlineServices,
        long activeCriticalAlerts,
        double avgLatencyMs,
        double kafkaMessagesProcessed,
        long openWorkflows
) {
}
