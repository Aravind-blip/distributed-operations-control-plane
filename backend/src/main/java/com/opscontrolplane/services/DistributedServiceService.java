package com.opscontrolplane.services;

import com.opscontrolplane.audit.AuditService;
import com.opscontrolplane.common.exception.ResourceNotFoundException;
import com.opscontrolplane.services.dto.CreateDistributedServiceRequest;
import com.opscontrolplane.services.dto.UpdateDistributedServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class DistributedServiceService {

    private final DistributedServiceRepository repository;
    private final AuditService auditService;

    public DistributedServiceService(DistributedServiceRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    public Page<DistributedService> list(Environment environment, ServiceStatus status, Pageable pageable) {
        Specification<DistributedService> spec = Specification.where(null);
        if (environment != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("environment"), environment));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        return repository.findAll(spec, pageable);
    }

    public DistributedService get(UUID id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("DistributedService", id));
    }

    public DistributedService create(CreateDistributedServiceRequest request, String actor) {
        DistributedService service = new DistributedService(
                request.name(), request.environment(), request.region(), request.status(),
                request.latencyMs(), request.errorRate(), Instant.now(), request.version(),
                request.ownerTeam(), request.serviceType());
        DistributedService saved = repository.save(service);
        auditService.record(actor, "SERVICE_CREATED", "DistributedService", saved.getId().toString(),
                "name=" + saved.getName());
        return saved;
    }

    public DistributedService update(UUID id, UpdateDistributedServiceRequest request, String actor) {
        DistributedService service = get(id);
        if (request.name() != null) service.setName(request.name());
        if (request.environment() != null) service.setEnvironment(request.environment());
        if (request.region() != null) service.setRegion(request.region());
        if (request.status() != null) service.setStatus(request.status());
        if (request.latencyMs() != null) service.setLatencyMs(request.latencyMs());
        if (request.errorRate() != null) service.setErrorRate(request.errorRate());
        if (request.version() != null) service.setVersion(request.version());
        if (request.ownerTeam() != null) service.setOwnerTeam(request.ownerTeam());
        if (request.serviceType() != null) service.setServiceType(request.serviceType());

        DistributedService saved = repository.save(service);
        auditService.record(actor, "SERVICE_UPDATED", "DistributedService", saved.getId().toString(),
                "name=" + saved.getName());
        return saved;
    }

    public void delete(UUID id, String actor) {
        DistributedService service = get(id);
        repository.delete(service);
        auditService.record(actor, "SERVICE_DELETED", "DistributedService", id.toString(),
                "name=" + service.getName());
    }
}
