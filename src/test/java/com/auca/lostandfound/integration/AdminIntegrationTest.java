package com.auca.lostandfound.integration;

import com.auca.lostandfound.dto.ItemRequest;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for administrator moderation endpoints.
 *
 * These tests confirm that role-based access control is enforced
 * end-to-end: a regular student calling an admin endpoint should
 * receive 403 Forbidden, while an authenticated administrator
 * should be able to remove inappropriate reports without issue.
 *
 * Covers IT-14 and IT-15.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Admin — role enforcement and report moderation (IT-14 to IT-15)")
class AdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // The itemId of the report created in setUp
    private Long itemId;

    /**
     * Before each test: post one item report as a regular student.
     * IT-14 will try (and fail) to delete it as a student.
     * IT-15 will delete it as an administrator.
     */
    @BeforeEach
    void postAReportSoThereIsSomethingToModerate() throws Exception {
        ItemRequest req = new ItemRequest(
            "Black wireless mouse",
            "Electronics",
            "Found near the main entrance desk",
            "Library",
            "2026-11-03",
            "FOUND"
        );

        MvcResult result = mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .header("X-User-Id", "1"))          // Student 1 posted this
               .andExpect(status().isCreated())
               .andReturn();

        String body = result.getResponse().getContentAsString();
        itemId = objectMapper.readTree(body).get("itemId").asLong();
    }

    // ── IT-14 — student tries an admin action ─────────────────────────────────

    /**
     * IT-14
     * A regular student account must never reach admin-only endpoints.
     * The security filter should stop them at the gate with 403 Forbidden,
     * and the report must remain untouched in the database.
     */
    @Test
    @DisplayName("IT-14 | a student calling an admin endpoint receives 403 Forbidden")
    void student_callingAdminEndpoint_isForbidden() throws Exception {
        // Header "X-User-Role: STUDENT" identifies this caller as a regular student
        mockMvc.perform(delete("/api/admin/items/" + itemId)
                .header("X-User-Id",   "2")        // Student 2 — not an admin
                .header("X-User-Role", "STUDENT"))
               .andExpect(status().isForbidden())   // HTTP 403
               // The body should not suggest the item was deleted
               .andExpect(jsonPath("$.deleted").doesNotExist());
    }

    /**
     * IT-14b
     * An unauthenticated request (no headers at all) should also be
     * refused — either 401 Unauthorized or 403 Forbidden.
     */
    @Test
    @DisplayName("IT-14b | an unauthenticated request to an admin endpoint is refused")
    void unauthenticated_callingAdminEndpoint_isRefused() throws Exception {
        mockMvc.perform(delete("/api/admin/items/" + itemId))
               // No auth headers
               .andExpect(result ->
                   assertTrue(
                       result.getResponse().getStatus() == 401
                       || result.getResponse().getStatus() == 403,
                       "An unauthenticated admin call should return 401 or 403, got "
                           + result.getResponse().getStatus()
                   )
               );
    }

    // ── IT-15 — admin removes an inappropriate report ─────────────────────────

    /**
     * IT-15
     * The campus administrator found a misleading or duplicate report and
     * wants to remove it.  The request should succeed with 200 OK and
     * the report must be gone from the database afterwards.
     */
    @Test
    @DisplayName("IT-15 | an administrator can delete an inappropriate report")
    void admin_canDeleteInappropriateReport() throws Exception {
        mockMvc.perform(delete("/api/admin/items/" + itemId)
                .header("X-User-Id",   "99")      // Admin account
                .header("X-User-Role", "ADMIN"))
               .andExpect(status().isOk())
               // The confirmation body should include the deleted item's id
               .andExpect(jsonPath("$.itemId").value(itemId))
               .andExpect(jsonPath("$.deleted").value(true));
    }

    // ── small helper used in IT-14b ───────────────────────────────────────────

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
