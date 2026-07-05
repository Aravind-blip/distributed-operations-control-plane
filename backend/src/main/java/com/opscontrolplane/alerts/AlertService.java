package com.opscontrolplane.alerts;

import com.opscontrolplane.audit.AuditService;
import com.opscontrolplane.common.exception.ResourceNotFoundException;
import com.opscontrolplane.metrics.OpsMetrics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final AuditService auditService;
    private final OpsMetrics metrics;

    public AlertService(AlertRepository alertRepository, AuditService auditService, OpsMetrics metrics) {
        this.alertRepository = alertRepository;
        this.auditService = auditService;
        this.metrics = metrics;
    }

    public Page<Alert> list(AlertSeverity severity, AlertStatus status, Pageable pageable) {
        Specification<Alert> spec = Specification.where(null);
        if (severity != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("severity"), severity));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        return alertRepository.findAll(spec, pageable);
    }

    public Alert get(UUID id) {
        return alertRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Alert", id));
    }

    public Alert create(AlertSeverity severity, UUID sourceServiceId, String title, String description, String actor) {
        Alert alert = new Alert(severity, sourceServiceId, title, description, AlertStatus.OPEN);
        Alert saved = alertRepository.save(alert);
        metrics.alertsCreated().increment();
        auditService.record(actor, "ALERT_CREATED", "Alert", saved.getId().toString(),
                "severity=" + severity + ", sourceServiceId=" + sourceServiceId);
        return saved;
    }

    public Alert acknowledge(UUID id, String acknowledgedBy, String actor) {
        return metrics.alertAcknowledgeTimer().record(() -> {
            Alert alert = get(id);
            alert.setStatus(AlertStatus.ACKNOWLEDGED);
            alert.setAcknowledgedBy(acknowledgedBy != null && !acknowledgedBy.isBlank() ? acknowledgedBy : actor);
            Alert saved = alertRepository.save(alert);
            metrics.alertsAcknowledged().increment();
            auditService.record(actor, "ALERT_ACKNOWLEDGED", "Alert", saved.getId().toString(),
                    "acknowledgedBy=" + saved.getAcknowledgedBy());
            return saved;
        });
    }

    public Alert resolve(UUID id, String actor) {
        return metrics.alertResolveTimer().record(() -> {
            Alert alert = get(id);
            alert.setStatus(AlertStatus.RESOLVED);
            alert.setResolvedBy(actor);
            alert.setResolvedAt(Instant.now());
            Alert saved = alertRepository.save(alert);
            metrics.alertsResolved().increment();
            auditService.record(actor, "ALERT_RESOLVED", "Alert", saved.getId().toString(),
                    "resolvedBy=" + saved.getResolvedBy());
            return saved;
        });
    }
}
