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

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkflowValidationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private String operatorToken;

    @BeforeEach
    void setUp() {
        TestUserFixtures.ensureUser(userRepository, passwordEncoder, "operator@ops.local", "operator123", Role.OPERATOR);
        operatorToken = jwtService.generateToken("operator@ops.local", "OPERATOR");
    }

    @Test
    void missingRiskLevelReturnsBadRequestWithValidationBody() throws Exception {
        String body = """
                {"workflowType": "RESTART_EDGE_GATEWAY", "requester": "operator@ops.local"}
                """;

        mockMvc.perform(post("/api/workflows")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("riskLevel"));
    }

    @Test
    void invalidWorkflowTypeReturnsBadRequest() throws Exception {
        String body = """
                {"workflowType": "NOT_A_REAL_TYPE", "requester": "operator@ops.local", "riskLevel": "LOW"}
                """;

        mockMvc.perform(post("/api/workflows")
                        .header("Authorization", "Bearer " + operatorToken)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
