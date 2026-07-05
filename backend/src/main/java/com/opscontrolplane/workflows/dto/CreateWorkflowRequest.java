package com.opscontrolplane.workflows.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * workflowType and riskLevel are intentionally Strings (not enums) so an invalid
 * value produces a normal Bean Validation 400 response instead of a Jackson
 * deserialization error; WorkflowService validates them against the allowed enum sets.
 */
public record CreateWorkflowRequest(
        @NotBlank String workflowType,
        @NotBlank String requester,
        @NotBlank String riskLevel
) {
}
