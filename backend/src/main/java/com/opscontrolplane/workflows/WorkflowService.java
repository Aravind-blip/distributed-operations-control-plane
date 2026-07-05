package com.opscontrolplane.workflows;

import com.opscontrolplane.audit.AuditService;
import com.opscontrolplane.common.exception.InvalidRequestException;
import com.opscontrolplane.common.exception.ResourceNotFoundException;
import com.opscontrolplane.kafka.KafkaTopics;
import com.opscontrolplane.kafka.event.WorkflowEvent;
import com.opscontrolplane.metrics.OpsMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class WorkflowService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowService.class);

    private final WorkflowRepository workflowRepository;
    private final AuditService auditService;
    private final OpsMetrics metrics;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public WorkflowService(WorkflowRepository workflowRepository, AuditService auditService, OpsMetrics metrics,
                            KafkaTemplate<String, Object> kafkaTemplate) {
        this.workflowRepository = workflowRepository;
        this.auditService = auditService;
        this.metrics = metrics;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Page<Workflow> list(Pageable pageable) {
        return workflowRepository.findAll(pageable);
    }

    public Workflow get(UUID id) {
        return workflowRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Workflow", id));
    }

    public Workflow create(String workflowTypeRaw, String requester, String riskLevelRaw, String actor) {
        WorkflowType workflowType = parseEnum(WorkflowType.class, workflowTypeRaw,
                "Invalid workflowType. Allowed values: " + java.util.Arrays.toString(WorkflowType.values()));
        RiskLevel riskLevel = parseEnum(RiskLevel.class, riskLevelRaw,
                "Invalid riskLevel. Allowed values: " + java.util.Arrays.toString(RiskLevel.values()));

        Workflow workflow = new Workflow(workflowType, requester, riskLevel);
        Workflow saved = workflowRepository.save(workflow);

        auditService.record(actor, "WORKFLOW_REQUESTED", "Workflow", saved.getId().toString(),
                "workflowType=" + workflowType + ", riskLevel=" + riskLevel);
        publishEvent(saved, actor);
        return saved;
    }

    public Workflow approve(UUID id, String approver) {
        return metrics.workflowApproveTimer().record(() -> {
            Workflow workflow = get(id);
            workflow.setStatus(WorkflowStatus.APPROVED);
            workflow.setApprover(approver);
            workflow.addAuditEntry("Approved by " + approver);
            Workflow saved = workflowRepository.save(workflow);
            metrics.workflowsApproved().increment();
            auditService.record(approver, "WORKFLOW_APPROVED", "Workflow", saved.getId().toString(),
                    "approver=" + approver);
            publishEvent(saved, approver);
            return saved;
        });
    }

    public Workflow reject(UUID id, String approver) {
        return metrics.workflowRejectTimer().record(() -> {
            Workflow workflow = get(id);
            workflow.setStatus(WorkflowStatus.REJECTED);
            workflow.setApprover(approver);
            workflow.addAuditEntry("Rejected by " + approver);
            Workflow saved = workflowRepository.save(workflow);
            metrics.workflowsRejected().increment();
            auditService.record(approver, "WORKFLOW_REJECTED", "Workflow", saved.getId().toString(),
                    "approver=" + approver);
            publishEvent(saved, approver);
            return saved;
        });
    }

    // Best-effort event propagation: the workflow's DB state and audit record
    // are already committed by the time this runs, so a Kafka outage must not
    // fail an otherwise-successful API call. Failures are logged and counted
    // instead of propagated.
    private void publishEvent(Workflow workflow, String actor) {
        try {
            WorkflowEvent event = new WorkflowEvent(workflow.getId(), workflow.getWorkflowType().name(),
                    workflow.getStatus().name(), actor, Instant.now());
            kafkaTemplate.send(KafkaTopics.WORKFLOW_EVENTS, workflow.getId().toString(), event);
        } catch (Exception ex) {
            metrics.kafkaMessagesFailed().increment();
            log.warn("Failed to publish workflow-events message for workflow={}", workflow.getId(), ex);
        }
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String raw, String errorMessage) {
        try {
            return Enum.valueOf(type, raw.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new InvalidRequestException(errorMessage);
        }
    }
}
