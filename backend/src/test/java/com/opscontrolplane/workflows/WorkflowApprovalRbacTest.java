package com.opscontrolplane.workflows;

import com.opscontrolplane.auth.JwtService;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkflowApprovalRbacTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private WorkflowRepository workflowRepository;

    private UUID workflowId;

    @BeforeEach
    void setUp() {
        TestUserFixtures.ensureUser(userRepository, passwordEncoder, "operator@ops.local", "operator123", Role.OPERATOR);
        TestUserFixtures.ensureUser(userRepository, passwordEncoder, "admin@ops.local", "admin123", Role.ADMIN);

        Workflow workflow = workflowRepository.save(
                new Workflow(WorkflowType.RESTART_EDGE_GATEWAY, "operator@ops.local", RiskLevel.HIGH));
        workflowId = workflow.getId();
    }

    @Test
    void operatorGetsForbiddenWhenApprovingWorkflow() throws Exception {
        String token = jwtService.generateToken("operator@ops.local", "OPERATOR");

        mockMvc.perform(post("/api/workflows/" + workflowId + "/approve")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanApproveWorkflow() throws Exception {
        String token = jwtService.generateToken("admin@ops.local", "ADMIN");

        mockMvc.perform(post("/api/workflows/" + workflowId + "/approve")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Workflow updated = workflowRepository.findById(workflowId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(WorkflowStatus.APPROVED);
        assertThat(updated.getApprover()).isEqualTo("admin@ops.local");
    }
}
