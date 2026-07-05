package com.opscontrolplane.workflows.dto;

import com.opscontrolplane.workflows.RiskLevel;
import com.opscontrolplane.workflows.Workflow;
import com.opscontrolplane.workflows.WorkflowStatus;
import com.opscontrolplane.workflows.WorkflowType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkflowResponse(
        UUID id,
        WorkflowType workflowType,
        String requester,
        String approver,
        RiskLevel riskLevel,
        WorkflowStatus status,
        Instant createdAt,
        Instant updatedAt,
        List<String> auditTrail
) {

    public static WorkflowResponse from(Workflow w) {
        return new WorkflowResponse(w.getId(), w.getWorkflowType(), w.getRequester(), w.getApprover(),
                w.getRiskLevel(), w.getStatus(), w.getCreatedAt(), w.getUpdatedAt(), List.copyOf(w.getAuditTrail()));
    }
}
