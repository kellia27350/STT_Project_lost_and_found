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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the claim workflow.
 *
 * Tests verify the full path from HTTP request down to H2 database:
 * creating valid claims, blocking claims on returned items, and
 * preventing duplicate active claims by the same student.
 *
 * Covers IT-08, IT-09, IT-10, IT-13.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Claims — submitting, blocking, and duplicate prevention (IT-08 to IT-10, IT-13)")
class ClaimIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // The itemId extracted after creating a Found report in setUp
    private Long foundItemId;

    /**
     * Before each test: post a Found item report so there is always
     * something for a student to claim.
     */
    @BeforeEach
    void postFoundItemSoThereIsSomethingToClaim() throws Exception {
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
                .header("X-User-Id", "1"))   // Student 1 posted the found item
               .andExpect(status().isCreated())
               .andReturn();

        // Pull the generated itemId from the response so claim tests can reference it
        String responseBody = result.getResponse().getContentAsString();
        foundItemId = objectMapper.readTree(responseBody).get("itemId").asLong();
    }

    // ── IT-08 — submit a valid claim ──────────────────────────────────────────

    /**
     * IT-08
     * Student 2 recognises the mouse as theirs and submits a claim.
     * The claim must be saved, linked to the correct item and student,
     * and have an ACTIVE status.
     */
    @Test
    @DisplayName("IT-08 | a student can claim an available Found item")
    void validClaim_isSavedAndLinkedToCorrectItemAndStudent() throws Exception {
        Map<String, String> claimPayload = Map.of(
            "explanation",
            "The mouse has a small blue sticker with my initials underneath."
        );

        mockMvc.perform(post("/api/claims/item/" + foundItemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(claimPayload))
                .header("X-User-Id", "2"))   // Student 2 is the claimant
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.itemId").value(foundItemId))
               .andExpect(jsonPath("$.claimantUserId").value(2))
               .andExpect(jsonPath("$.claimStatus").value("ACTIVE"));
    }

    // ── IT-09 — claim a Returned item ─────────────────────────────────────────

    /**
     * IT-09
     * Once an item has been handed back to its owner (status = RETURNED)
     * the chapter is closed.  Any new claim must be rejected so the
     * database stays consistent.
     */
    @Test
    @DisplayName("IT-09 | claiming an already-returned item is rejected")
    void claimOnReturnedItem_isRejected() throws Exception {
        // First, an admin/owner marks the mouse as RETURNED
        mockMvc.perform(patch("/api/items/" + foundItemId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "RETURNED")))
                .header("X-User-Id", "1"))   // the report owner
               .andExpect(status().isOk());

        // Now Student 2 tries to claim the same item — should be blocked
        Map<String, String> claimPayload = Map.of(
            "explanation",
            "The mouse has a small blue sticker with my initials underneath."
        );

        mockMvc.perform(post("/api/claims/item/" + foundItemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(claimPayload))
                .header("X-User-Id", "2"))
               .andExpect(status().is4xxClientError()); // 400 or 409
    }

    // ── IT-10 — mark an item as Returned ──────────────────────────────────────

    /**
     * IT-10
     * After the finder and the owner meet and hand the item over, the
     * report owner (or an admin) updates the status to RETURNED.
     * The database must reflect this immediately.
     */
    @Test
    @DisplayName("IT-10 | marking an item as Returned updates its status in the database")
    void markItemReturned_updatesStatusInDatabase() throws Exception {
        mockMvc.perform(patch("/api/items/" + foundItemId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("status", "RETURNED")))
                .header("X-User-Id", "1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("RETURNED"))
               .andExpect(jsonPath("$.itemId").value(foundItemId));
    }

    // ── IT-13 — duplicate active claim ────────────────────────────────────────

    /**
     * IT-13
     * Student 2 already submitted a claim on this item (see IT-08).
     * Submitting a second claim while the first is still ACTIVE must
     * be blocked — only one active claim per student per item is allowed.
     */
    @Test
    @DisplayName("IT-13 | a student cannot submit a second claim while their first is still active")
    void duplicateActiveClaim_isRejectedAndOnlyOneClaimExists() throws Exception {
        Map<String, String> claimPayload = Map.of(
            "explanation",
            "The mouse has a small blue sticker with my initials underneath."
        );

        // First claim — should succeed
        mockMvc.perform(post("/api/claims/item/" + foundItemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(claimPayload))
                .header("X-User-Id", "2"))
               .andExpect(status().isCreated());

        // Second claim from the same student — must be rejected
        mockMvc.perform(post("/api/claims/item/" + foundItemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(claimPayload))
                .header("X-User-Id", "2"))
               .andExpect(status().is4xxClientError()); // 400 or 409
    }
}
