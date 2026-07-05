package com.opscontrolplane.alerts;

import com.opscontrolplane.alerts.dto.AcknowledgeAlertRequest;
import com.opscontrolplane.alerts.dto.AlertResponse;
import com.opscontrolplane.alerts.dto.CreateAlertRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public Page<AlertResponse> list(@RequestParam(required = false) AlertSeverity severity,
                                     @RequestParam(required = false) AlertStatus status,
                                     @RequestParam(defaultValue = "20") int limit,
                                     @RequestParam(defaultValue = "0") int offset) {
        int page = offset / Math.max(limit, 1);
        return alertService.list(severity, status, PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(AlertResponse::from);
    }

    @GetMapping("/{id}")
    public AlertResponse get(@PathVariable UUID id) {
        return AlertResponse.from(alertService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlertResponse> create(@Valid @RequestBody CreateAlertRequest request, Authentication auth) {
        Alert alert = alertService.create(request.severity(), request.sourceServiceId(), request.title(),
                request.description(), auth.getName());
        return ResponseEntity.ok(AlertResponse.from(alert));
    }

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public AlertResponse acknowledge(@PathVariable UUID id,
                                      @RequestBody(required = false) AcknowledgeAlertRequest request,
                                      Authentication auth) {
        String acknowledgedBy = request != null ? request.acknowledgedBy() : null;
        return AlertResponse.from(alertService.acknowledge(id, acknowledgedBy, auth.getName()));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public AlertResponse resolve(@PathVariable UUID id, Authentication auth) {
        return AlertResponse.from(alertService.resolve(id, auth.getName()));
    }
}
