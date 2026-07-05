package com.opscontrolplane.metrics;

import com.opscontrolplane.metrics.dto.DashboardSummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetricsController {

    private final DashboardMetricsService dashboardMetricsService;

    public MetricsController(DashboardMetricsService dashboardMetricsService) {
        this.dashboardMetricsService = dashboardMetricsService;
    }

    @GetMapping("/api/metrics/dashboard-summary")
    public DashboardSummaryResponse dashboardSummary() {
        return dashboardMetricsService.summary();
    }
}
