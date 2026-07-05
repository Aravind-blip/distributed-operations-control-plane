package com.opscontrolplane.alerts;

import com.opscontrolplane.audit.AuditService;
import com.opscontrolplane.metrics.OpsMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceUnitTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AuditService auditService;

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        OpsMetrics metrics = new OpsMetrics(new SimpleMeterRegistry());
        alertService = new AlertService(alertRepository, auditService, metrics);
    }

    @Test
    void acknowledgeDefaultsToActorEmailWhenAcknowledgedByBlank() throws Exception {
        UUID id = UUID.randomUUID();
        Alert alert = new Alert(AlertSeverity.HIGH, UUID.randomUUID(), "title", "desc", AlertStatus.OPEN);
        setId(alert, id);
        when(alertRepository.findById(id)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        Alert result = alertService.acknowledge(id, "  ", "operator@ops.local");

        assertThat(result.getStatus()).isEqualTo(AlertStatus.ACKNOWLEDGED);
        assertThat(result.getAcknowledgedBy()).isEqualTo("operator@ops.local");
        verify(auditService).record("operator@ops.local", "ALERT_ACKNOWLEDGED", "Alert", id.toString(),
                "acknowledgedBy=operator@ops.local");
    }

    @Test
    void resolveSetsResolvedByAndTimestamp() throws Exception {
        UUID id = UUID.randomUUID();
        Alert alert = new Alert(AlertSeverity.CRITICAL, UUID.randomUUID(), "title", "desc", AlertStatus.ACKNOWLEDGED);
        setId(alert, id);
        when(alertRepository.findById(id)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        Alert result = alertService.resolve(id, "admin@ops.local");

        assertThat(result.getStatus()).isEqualTo(AlertStatus.RESOLVED);
        assertThat(result.getResolvedBy()).isEqualTo("admin@ops.local");
        assertThat(result.getResolvedAt()).isNotNull();
    }

    private static void setId(Alert alert, UUID id) throws Exception {
        Field field = Alert.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(alert, id);
    }
}
