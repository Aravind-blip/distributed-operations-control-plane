package com.opscontrolplane.workflows;

import com.opscontrolplane.audit.AuditService;
import com.opscontrolplane.common.exception.InvalidRequestException;
import com.opscontrolplane.metrics.OpsMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceUnitTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private WorkflowService workflowService;

    @BeforeEach
    void setUp() {
        OpsMetrics metrics = new OpsMetrics(new SimpleMeterRegistry());
        workflowService = new WorkflowService(workflowRepository, auditService, metrics, kafkaTemplate);
    }

    @Test
    void createRejectsInvalidWorkflowType() {
        assertThatThrownBy(() -> workflowService.create("NOT_A_TYPE", "operator@ops.local", "LOW", "operator@ops.local"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Invalid workflowType");
    }

    @Test
    void createRejectsInvalidRiskLevel() {
        assertThatThrownBy(() -> workflowService.create("INCIDENT_REVIEW", "operator@ops.local", "SUPER_HIGH", "operator@ops.local"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Invalid riskLevel");
    }

    @Test
    void createPersistsWorkflowForValidInput() {
        when(workflowRepository.save(any(Workflow.class))).thenAnswer(inv -> inv.getArgument(0));

        Workflow workflow = workflowService.create("INCIDENT_REVIEW", "operator@ops.local", "MEDIUM", "operator@ops.local");

        assertThat(workflow.getWorkflowType()).isEqualTo(WorkflowType.INCIDENT_REVIEW);
        assertThat(workflow.getRiskLevel()).isEqualTo(RiskLevel.MEDIUM);
        assertThat(workflow.getStatus()).isEqualTo(WorkflowStatus.REQUESTED);
    }
}
