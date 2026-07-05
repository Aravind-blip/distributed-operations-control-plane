package com.opscontrolplane.alerts;

import com.opscontrolplane.audit.AuditLogRepository;
import com.opscontrolplane.auth.JwtService;
import com.opscontrolplane.services.DistributedService;
import com.opscontrolplane.services.DistributedServiceRepository;
import com.opscontrolplane.services.Environment;
import com.opscontrolplane.services.ServiceStatus;
import com.opscontrolplane.support.TestUserFixtures;
import com.opscontrolplane.users.Role;
import com.opscontrolplane.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlertAcknowledgeRbacTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private DistributedServiceRepository serviceRepository;
    @Autowired
    private AlertRepository alertRepository;
    @Autowired
    private AuditLogRepository auditLogRepository;

    private UUID alertId;

    @BeforeEach
    void setUp() {
        TestUserFixtures.ensureUser(userRepository, passwordEncoder, "viewer@ops.local", "viewer123", Role.VIEWER);
        TestUserFixtures.ensureUser(userRepository, passwordEncoder, "operator@ops.local", "operator123", Role.OPERATOR);

        DistributedService service = serviceRepository.save(new DistributedService(
                "Test Service", Environment.CLOUD, "us-east-1", ServiceStatus.DEGRADED, 300.0, 0.05,
                Instant.now(), "1.0.0", "Test Team", "REST API"));

        Alert alert = alertRepository.save(new Alert(AlertSeverity.HIGH, service.getId(), "Test alert",
                "Test alert description", AlertStatus.OPEN));
        alertId = alert.getId();
    }

    @Test
    void viewerGetsForbiddenWhenAcknowledgingAlert() throws Exception {
        String token = jwtService.generateToken("viewer@ops.local", "VIEWER");

        mockMvc.perform(post("/api/alerts/" + alertId + "/acknowledge")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void operatorCanAcknowledgeAlert() throws Exception {
        String token = jwtService.generateToken("operator@ops.local", "OPERATOR");

        mockMvc.perform(post("/api/alerts/" + alertId + "/acknowledge")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Alert updated = alertRepository.findById(alertId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(AlertStatus.ACKNOWLEDGED);
    }

    @Test
    void auditLogRowIsCreatedAfterAlertAcknowledgement() throws Exception {
        long before = auditLogRepository.count();
        String token = jwtService.generateToken("operator@ops.local", "OPERATOR");

        mockMvc.perform(post("/api/alerts/" + alertId + "/acknowledge")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        long after = auditLogRepository.count();
        assertThat(after).isGreaterThan(before);
        assertThat(auditLogRepository.findAll().stream()
                .anyMatch(log -> log.getAction().equals("ALERT_ACKNOWLEDGED") && log.getResourceId().equals(alertId.toString())))
                .isTrue();
    }
}
