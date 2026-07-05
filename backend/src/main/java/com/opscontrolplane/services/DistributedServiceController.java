package com.opscontrolplane.services;

import com.opscontrolplane.services.dto.CreateDistributedServiceRequest;
import com.opscontrolplane.services.dto.DistributedServiceResponse;
import com.opscontrolplane.services.dto.UpdateDistributedServiceRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/services")
public class DistributedServiceController {

    private final DistributedServiceService service;

    public DistributedServiceController(DistributedServiceService service) {
        this.service = service;
    }

    @GetMapping
    public Page<DistributedServiceResponse> list(@RequestParam(required = false) Environment environment,
                                                  @RequestParam(required = false) ServiceStatus status,
                                                  @RequestParam(defaultValue = "20") int limit,
                                                  @RequestParam(defaultValue = "0") int offset) {
        int page = offset / Math.max(limit, 1);
        return service.list(environment, status, PageRequest.of(page, limit, Sort.by(Sort.Direction.ASC, "name")))
                .map(DistributedServiceResponse::from);
    }

    @GetMapping("/{id}")
    public DistributedServiceResponse get(@PathVariable UUID id) {
        return DistributedServiceResponse.from(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DistributedServiceResponse> create(@Valid @RequestBody CreateDistributedServiceRequest request,
                                                              Authentication auth) {
        return ResponseEntity.ok(DistributedServiceResponse.from(service.create(request, auth.getName())));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DistributedServiceResponse update(@PathVariable UUID id,
                                              @RequestBody UpdateDistributedServiceRequest request,
                                              Authentication auth) {
        return DistributedServiceResponse.from(service.update(id, request, auth.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication auth) {
        service.delete(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
