package com.opscontrolplane.audit;

import org.springframework.stereotype.Service;

/**
 * Central write-path for audit trail entries. Other modules call this after every
 * mutation (alert ack/resolve, workflow approve/reject, service CRUD) so there is a
 * single, consistent place that decides how audit rows are shaped and persisted.
 */
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public AuditLog record(String actor, String action, String resourceType, String resourceId, String metadata) {
        AuditLog log = new AuditLog(actor, action, resourceType, resourceId, metadata);
        return auditLogRepository.save(log);
    }
}
