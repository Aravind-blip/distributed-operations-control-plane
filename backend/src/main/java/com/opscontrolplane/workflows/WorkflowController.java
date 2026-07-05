package com.opscontrolplane.workflows;

import com.opscontrolplane.workflows.dto.CreateWorkflowRequest;
import com.opscontrolplane.workflows.dto.WorkflowResponse;
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
@RequestMapping("/api/workflows")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    public Page<WorkflowResponse> list(@RequestParam(defaultValue = "20") int limit,
                                        @RequestParam(defaultValue = "0") int offset) {
        int page = offset / Math.max(limit, 1);
        return workflowService.list(PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(WorkflowResponse::from);
    }

    @GetMapping("/{id}")
    public WorkflowResponse get(@PathVariable UUID id) {
        return WorkflowResponse.from(workflowService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<WorkflowResponse> create(@Valid @RequestBody CreateWorkflowRequest request, Authentication auth) {
        Workflow workflow = workflowService.create(request.workflowType(), request.requester(), request.riskLevel(),
                auth.getName());
        return ResponseEntity.ok(WorkflowResponse.from(workflow));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public WorkflowResponse approve(@PathVariable UUID id, Authentication auth) {
        return WorkflowResponse.from(workflowService.approve(id, auth.getName()));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public WorkflowResponse reject(@PathVariable UUID id, Authentication auth) {
        return WorkflowResponse.from(workflowService.reject(id, auth.getName()));
    }
}
