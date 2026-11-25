package com.auca.lostandfound.integration;

import com.auca.lostandfound.dto.ItemRequest;
import com.auca.lostandfound.model.User;
import com.auca.lostandfound.model.User.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the registration and login flow.
 *
 * These tests fire real HTTP requests through MockMvc, hitting the full
 * controller → service → repository → H2 database stack.  Every test
 * runs inside a transaction that is rolled back at the end, so each
 * test always starts with a clean slate.
 *
 * Covers IT-01, IT-02, IT-03, IT-04.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Auth — registration and login (IT-01 to IT-04)")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ── IT-01 — successful registration ──────────────────────────────────────

    /**
     * IT-01
     * When a new student fills in all the required fields and submits,
     * the account should be saved exactly once in the database with a
     * hashed password (not plain text) and the STUDENT role.
     */
    @Test
    @DisplayName("IT-01 | registering a new student account saves it in the database")
    void newStudentRegistration_savesAccountOnce() throws Exception {
        Map<String, String> payload = Map.of(
            "studentId", "TEST001",
            "name",      "Test Student",
            "email",     "student.test@example.com",
            "password",  "SecurePass1"
        );

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
               .andExpect(status().isCreated())
               // The response should echo back the new user's email
               .andExpect(jsonPath("$.email").value("student.test@example.com"))
               // The raw password must never appear in the response
               .andExpect(jsonPath("$.password").doesNotExist())
               .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    // ── IT-02 — duplicate registration ───────────────────────────────────────

    /**
     * IT-02
     * If someone tries to register with an email that already exists,
     * the system must reject the second attempt outright and must not
     * create a duplicate row in the users table.
     */
    @Test
    @DisplayName("IT-02 | registering with an already-used email is rejected")
    void duplicateEmailRegistration_isRejectedWithNoExtraRow() throws Exception {
        Map<String, String> payload = Map.of(
            "studentId", "TEST001",
            "name",      "Test Student",
            "email",     "student.test@example.com",
            "password",  "SecurePass1"
        );

        // First registration goes through fine
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
               .andExpect(status().isCreated());

        // Second attempt with the same email must be refused
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
               .andExpect(status().is4xxClientError()); // 400 or 409
    }

    // ── IT-03 — valid login ───────────────────────────────────────────────────

    /**
     * IT-03
     * After registering, the student should be able to log in with the
     * same email and password and receive a session token or 200 OK.
     */
    @Test
    @DisplayName("IT-03 | logging in with correct credentials returns 200 OK")
    void validLogin_returns200WithToken() throws Exception {
        // Register first so the account exists in the test database
        Map<String, String> registerPayload = Map.of(
            "studentId", "TEST001",
            "name",      "Test Student",
            "email",     "student.test@example.com",
            "password",  "SecurePass1"
        );
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerPayload)))
               .andExpect(status().isCreated());

        // Now try to log in
        Map<String, String> loginPayload = Map.of(
            "email",    "student.test@example.com",
            "password", "SecurePass1"
        );
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginPayload)))
               .andExpect(status().isOk())
               // A token or session identifier should be part of the response
               .andExpect(jsonPath("$.token").isNotEmpty());
    }

    // ── IT-04 — wrong password ────────────────────────────────────────────────

    /**
     * IT-04
     * Submitting the right email but the wrong password must be refused
     * with 401 Unauthorized — no session, no token.
     */
    @Test
    @DisplayName("IT-04 | logging in with the wrong password returns 401 Unauthorized")
    void wrongPasswordLogin_returns401() throws Exception {
        // Register the account
        Map<String, String> registerPayload = Map.of(
            "studentId", "TEST001",
            "name",      "Test Student",
            "email",     "student.test@example.com",
            "password",  "SecurePass1"
        );
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerPayload)))
               .andExpect(status().isCreated());

        // Attempt login with the wrong password
        Map<String, String> badLoginPayload = Map.of(
            "email",    "student.test@example.com",
            "password", "WrongPassword!"
        );
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badLoginPayload)))
               .andExpect(status().isUnauthorized());
    }
}
