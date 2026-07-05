package com.opscontrolplane.config;

import com.opscontrolplane.alerts.Alert;
import com.opscontrolplane.alerts.AlertRepository;
import com.opscontrolplane.alerts.AlertSeverity;
import com.opscontrolplane.alerts.AlertStatus;
import com.opscontrolplane.audit.AuditLogRepository;
import com.opscontrolplane.audit.AuditService;
import com.opscontrolplane.services.DistributedService;
import com.opscontrolplane.services.DistributedServiceRepository;
import com.opscontrolplane.services.Environment;
import com.opscontrolplane.services.ServiceStatus;
import com.opscontrolplane.users.Role;
import com.opscontrolplane.users.User;
import com.opscontrolplane.users.UserRepository;
import com.opscontrolplane.workflows.RiskLevel;
import com.opscontrolplane.workflows.Workflow;
import com.opscontrolplane.workflows.WorkflowRepository;
import com.opscontrolplane.workflows.WorkflowStatus;
import com.opscontrolplane.workflows.WorkflowType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Seeds baseline demo data on first startup only (guarded by an empty-users check), so
 * this is safe to leave enabled across restarts without duplicating rows.
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DistributedServiceRepository serviceRepository;
    private final AlertRepository alertRepository;
    private final WorkflowRepository workflowRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, DistributedServiceRepository serviceRepository,
                       AlertRepository alertRepository, WorkflowRepository workflowRepository,
                       AuditService auditService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.alertRepository = alertRepository;
        this.workflowRepository = workflowRepository;
        this.auditService = auditService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.findAll().isEmpty()) {
            return;
        }

        seedUsers();
        List<DistributedService> services = seedServices();
        seedAlerts(services);
        seedWorkflows();
        seedAuditLogs();
    }

    private void seedUsers() {
        userRepository.save(new User("admin@ops.local", passwordEncoder.encode("admin123"), Role.ADMIN));
        userRepository.save(new User("operator@ops.local", passwordEncoder.encode("operator123"), Role.OPERATOR));
        userRepository.save(new User("viewer@ops.local", passwordEncoder.encode("viewer123"), Role.VIEWER));
    }

    private List<DistributedService> seedServices() {
        Instant now = Instant.now();
        record Seed(String name, Environment env, String region, ServiceStatus status, double latency,
                    double errorRate, String version, String team, String type) {
        }

        List<Seed> seeds = List.of(
                new Seed("Catalog Service", Environment.CLOUD, "us-east-1", ServiceStatus.HEALTHY, 45.2, 0.002, "2.3.1", "Commerce Platform", "REST API"),
                new Seed("Order Orchestration Service", Environment.CLOUD, "us-east-1", ServiceStatus.HEALTHY, 62.8, 0.004, "1.9.0", "Commerce Platform", "Orchestrator"),
                new Seed("Entitlement Service", Environment.CLOUD, "us-west-2", ServiceStatus.DEGRADED, 310.5, 0.08, "3.1.2", "Identity & Access", "REST API"),
                new Seed("Billing Event Publisher", Environment.CLOUD, "us-east-1", ServiceStatus.HEALTHY, 55.1, 0.001, "1.4.4", "Billing Platform", "Kafka Producer"),
                new Seed("Usage Metering Processor", Environment.DATA_CENTER, "dc-central-1", ServiceStatus.HEALTHY, 120.7, 0.006, "2.0.5", "Billing Platform", "Batch Processor"),
                new Seed("Notification Service", Environment.CLOUD, "eu-west-1", ServiceStatus.HEALTHY, 38.9, 0.003, "4.2.0", "Platform Services", "REST API"),
                new Seed("Customer Visibility API", Environment.CLOUD, "us-east-1", ServiceStatus.HEALTHY, 71.3, 0.005, "1.1.9", "Customer Experience", "GraphQL API"),
                new Seed("Telemetry Collector", Environment.EDGE, "edge-region-3", ServiceStatus.HEALTHY, 15.4, 0.001, "0.9.8", "SRE Platform", "Agent"),
                new Seed("Edge Gateway", Environment.EDGE, "edge-region-3", ServiceStatus.OFFLINE, 1450.0, 0.92, "5.6.1", "Network Engineering", "Gateway"),
                new Seed("Data Center Batch Processor", Environment.DATA_CENTER, "dc-central-1", ServiceStatus.HEALTHY, 205.6, 0.01, "3.3.3", "Data Platform", "Batch Processor"),
                new Seed("Risk Scoring Service", Environment.CLOUD, "us-west-2", ServiceStatus.DEGRADED, 420.2, 0.11, "2.7.0", "Risk & Fraud", "ML Inference"),
                new Seed("Kafka Event Processor", Environment.CLOUD, "us-east-1", ServiceStatus.HEALTHY, 28.6, 0.002, "1.6.2", "Platform Services", "Stream Processor"),
                new Seed("Legacy SOAP Adapter", Environment.DATA_CENTER, "dc-central-1", ServiceStatus.DEGRADED, 640.9, 0.15, "0.8.1", "Legacy Integration", "SOAP Bridge"),
                new Seed("EMS Message Bridge", Environment.DATA_CENTER, "dc-central-1", ServiceStatus.HEALTHY, 95.4, 0.007, "0.6.3", "Legacy Integration", "EMS Bridge")
        );

        return seeds.stream()
                .map(s -> serviceRepository.save(new DistributedService(
                        s.name(), s.env(), s.region(), s.status(), s.latency(), s.errorRate(),
                        now.minus((long) (Math.random() * 120), ChronoUnit.SECONDS), s.version(), s.team(), s.type())))
                .toList();
    }

    private void seedAlerts(List<DistributedService> services) {
        DistributedService entitlement = findByName(services, "Entitlement Service");
        DistributedService edgeGateway = findByName(services, "Edge Gateway");
        DistributedService riskScoring = findByName(services, "Risk Scoring Service");
        DistributedService soapAdapter = findByName(services, "Legacy SOAP Adapter");

        alertRepository.save(new Alert(AlertSeverity.CRITICAL, edgeGateway.getId(), "Edge Gateway is OFFLINE",
                "Edge Gateway has stopped responding to health checks in edge-region-3.", AlertStatus.OPEN));
        alertRepository.save(new Alert(AlertSeverity.HIGH, entitlement.getId(), "Entitlement Service latency spike",
                "p99 latency exceeded 300ms threshold for Entitlement Service.", AlertStatus.ACKNOWLEDGED));
        alertRepository.save(new Alert(AlertSeverity.MEDIUM, riskScoring.getId(), "Risk Scoring error rate elevated",
                "Error rate for Risk Scoring Service crossed 10% over a 5 minute window.", AlertStatus.OPEN));
        alertRepository.save(new Alert(AlertSeverity.LOW, soapAdapter.getId(), "Legacy SOAP Adapter degraded",
                "Legacy SOAP Adapter response times trending upward.", AlertStatus.RESOLVED));
    }

    private void seedWorkflows() {
        Workflow w1 = new Workflow(WorkflowType.RESTART_EDGE_GATEWAY, "operator@ops.local", RiskLevel.HIGH);
        workflowRepository.save(w1);

        Workflow w2 = new Workflow(WorkflowType.INCIDENT_REVIEW, "operator@ops.local", RiskLevel.MEDIUM);
        w2.setStatus(WorkflowStatus.APPROVED);
        w2.setApprover("admin@ops.local");
        w2.addAuditEntry("Approved by admin@ops.local");
        workflowRepository.save(w2);

        Workflow w3 = new Workflow(WorkflowType.VALIDATE_SOAP_ADAPTER_RECOVERY, "operator@ops.local", RiskLevel.LOW);
        w3.setStatus(WorkflowStatus.COMPLETED);
        w3.setApprover("admin@ops.local");
        w3.addAuditEntry("Approved by admin@ops.local");
        w3.addAuditEntry("Marked complete after validation");
        workflowRepository.save(w3);

        Workflow w4 = new Workflow(WorkflowType.SCALE_K8S_DEPLOYMENT, "operator@ops.local", RiskLevel.MEDIUM);
        w4.setStatus(WorkflowStatus.REJECTED);
        w4.setApprover("admin@ops.local");
        w4.addAuditEntry("Rejected by admin@ops.local: insufficient capacity headroom");
        workflowRepository.save(w4);
    }

    private void seedAuditLogs() {
        auditService.record("system", "SEED_INITIALIZED", "System", "startup", "Initial demo dataset loaded");
        auditService.record("admin@ops.local", "USER_LOGIN", "User", "admin@ops.local", Map.of("note", "seed login example").toString());
        auditService.record("operator@ops.local", "WORKFLOW_REQUESTED", "Workflow", "seed-workflow", "Initial seed data audit entry");
    }

    private static DistributedService findByName(List<DistributedService> services, String name) {
        return services.stream().filter(s -> s.getName().equals(name)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Seed service not found: " + name));
    }
}
