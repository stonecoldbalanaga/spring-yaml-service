package com.example.yamlservice;

import com.example.yamlservice.model.AuthRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AppValuesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ------------------------------------------------------------------
    // Auth endpoint tests
    // ------------------------------------------------------------------

    @Test
    void loginWithValidCredentialsReturnsToken() throws Exception {
        AuthRequest req = new AuthRequest("admin", "admin123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    void loginWithInvalidCredentialsReturns401() throws Exception {
        AuthRequest req = new AuthRequest("admin", "wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithBlankUsernameReturns400() throws Exception {
        AuthRequest req = new AuthRequest("", "admin123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------
    // AppValues endpoint tests
    // ------------------------------------------------------------------

    @Test
    void getAllValuesWithoutTokenReturns403() throws Exception {
        mockMvc.perform(get("/api/values"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllValuesWithValidTokenReturnsEntries() throws Exception {
        String token = obtainToken("user", "user123");
        mockMvc.perform(get("/api/values")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['app.name']").value("YamlMicroservice"));
    }

    @Test
    void getValueByKeyReturnsCorrectEntry() throws Exception {
        String token = obtainToken("user", "user123");
        mockMvc.perform(get("/api/values/app.version")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['app.version']").value("1.0.0"));
    }

    @Test
    void getValueByMissingKeyReturns422() throws Exception {
        String token = obtainToken("user", "user123");
        mockMvc.perform(get("/api/values/does.not.exist")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("YAML Validation Error"));
    }

    @Test
    void reloadEndpointForbiddenForUserRole() throws Exception {
        String token = obtainToken("user", "user123");
        mockMvc.perform(get("/api/values/reload")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void reloadEndpointSucceedsForAdminRole() throws Exception {
        String token = obtainToken("admin", "admin123");
        mockMvc.perform(get("/api/values/reload")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.entriesLoaded", greaterThan(0)));
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private String obtainToken(String username, String password) throws Exception {
        AuthRequest req = new AuthRequest(username, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }
}
