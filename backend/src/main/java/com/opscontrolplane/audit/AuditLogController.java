package com.opscontrolplane.audit;

import com.opscontrolplane.audit.dto.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/api/audit-logs")
    public Page<AuditLogResponse> list(@RequestParam(defaultValue = "20") int limit,
                                        @RequestParam(defaultValue = "0") int offset) {
        int page = offset / Math.max(limit, 1);
        return auditLogRepository
                .findAll(PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(AuditLogResponse::from);
    }
}
