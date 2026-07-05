package com.opscontrolplane.workflows;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workflows")
public class Workflow {

    // Generated client-side (rather than @GeneratedValue) so the ID is available
    // immediately after construction -- needed to publish the Kafka event and
    // write the audit log entry in the same service call that persists the row.
    @Id
    private UUID id = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowType workflowType;

    @Column(nullable = false)
    private String requester;

    private String approver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    // EAGER: small, bounded collection that's always needed by WorkflowResponse;
    // avoids LazyInitializationException when serialized outside the request's
    // transaction (there's no open-session-in-view for API responses here).
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "workflow_audit_trail", joinColumns = @JoinColumn(name = "workflow_id"))
    @OrderColumn(name = "entry_order")
    @Column(name = "entry", length = 1000)
    private List<String> auditTrail = new ArrayList<>();

    protected Workflow() {
    }

    public Workflow(WorkflowType workflowType, String requester, RiskLevel riskLevel) {
        this.workflowType = workflowType;
        this.requester = requester;
        this.riskLevel = riskLevel;
        this.status = WorkflowStatus.REQUESTED;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        addAuditEntry("Workflow requested by " + requester);
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void addAuditEntry(String text) {
        auditTrail.add(Instant.now() + " - " + text);
    }

    public UUID getId() {
        return id;
    }

    public WorkflowType getWorkflowType() {
        return workflowType;
    }

    public String getRequester() {
        return requester;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public WorkflowStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<String> getAuditTrail() {
        return auditTrail;
    }
}
