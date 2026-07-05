package com.opscontrolplane.metrics;

import com.opscontrolplane.alerts.AlertRepository;
import com.opscontrolplane.alerts.AlertSeverity;
import com.opscontrolplane.alerts.AlertStatus;
import com.opscontrolplane.metrics.dto.DashboardSummaryResponse;
import com.opscontrolplane.services.DistributedService;
import com.opscontrolplane.services.DistributedServiceRepository;
import com.opscontrolplane.services.ServiceStatus;
import com.opscontrolplane.workflows.WorkflowRepository;
import com.opscontrolplane.workflows.WorkflowStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardMetricsService {

    private final DistributedServiceRepository serviceRepository;
    private final AlertRepository alertRepository;
    private final WorkflowRepository workflowRepository;
    private final OpsMetrics opsMetrics;

    public DashboardMetricsService(DistributedServiceRepository serviceRepository, AlertRepository alertRepository,
                                    WorkflowRepository workflowRepository, OpsMetrics opsMetrics) {
        this.serviceRepository = serviceRepository;
        this.alertRepository = alertRepository;
        this.workflowRepository = workflowRepository;
        this.opsMetrics = opsMetrics;
    }

    public DashboardSummaryResponse summary() {
        List<DistributedService> services = serviceRepository.findAll();
        long total = services.size();
        long healthy = services.stream().filter(s -> s.getStatus() == ServiceStatus.HEALTHY).count();
        long degraded = services.stream().filter(s -> s.getStatus() == ServiceStatus.DEGRADED).count();
        long offline = services.stream().filter(s -> s.getStatus() == ServiceStatus.OFFLINE).count();
        double avgLatency = services.stream().mapToDouble(DistributedService::getLatencyMs).average().orElse(0.0);

        long activeCritical = alertRepository.countByStatusAndSeverity(AlertStatus.OPEN, AlertSeverity.CRITICAL)
                + alertRepository.countByStatusAndSeverity(AlertStatus.ACKNOWLEDGED, AlertSeverity.CRITICAL);

        long openWorkflows = workflowRepository.countByStatus(WorkflowStatus.REQUESTED);

        double kafkaProcessed = opsMetrics.kafkaMessagesProcessed().count();

        return new DashboardSummaryResponse(total, healthy, degraded, offline, activeCritical, avgLatency,
                kafkaProcessed, openWorkflows);
    }
}
