package com.auca.lostandfound.unit;

import com.auca.lostandfound.model.Claim;
import com.auca.lostandfound.model.Item;
import com.auca.lostandfound.service.ClaimService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ClaimService.
 *
 * Covers UT-10 through UT-14 and UT-17:
 *   whether a claim can be opened on a given item, explanation text,
 *   and duplicate-claim prevention.
 *
 * No Spring context or database — the service is instantiated directly
 * and all test data is built in memory.
 */
@DisplayName("ClaimService — claim eligibility and duplicate prevention")
class ClaimServiceTest {

    private ClaimService claimService;

    @BeforeEach
    void setUp() {
        claimService = new ClaimService();
    }

    // ── can a claim be opened? ────────────────────────────────────────────────

    /**
     * UT-10
     * This is the happy path: a Found item that is still available
     * is exactly what a claim is designed for.
     */
    @Test
    @DisplayName("UT-10 | a Found + Available item can be claimed")
    void foundAndAvailableItem_canBeClaimed() {
        Item item = buildItem("FOUND", "AVAILABLE");
        assertTrue(claimService.canClaim(item),
            "A FOUND item with AVAILABLE status should allow a claim.");
    }

    /**
     * UT-11
     * A Lost item is a report posted by the person who lost something.
     * There is no finder to claim from, so claims must be blocked.
     */
    @Test
    @DisplayName("UT-11 | a Lost item cannot be claimed — it is not a found report")
    void lostItem_cannotBeClaimed() {
        Item item = buildItem("LOST", "AVAILABLE");
        assertFalse(claimService.canClaim(item),
            "Claims on LOST items make no sense and should return false.");
    }

    /**
     * UT-12
     * Once an item is marked RETURNED the handover is complete.
     * Opening a new claim at that point would be pointless.
     */
    @Test
    @DisplayName("UT-12 | a Returned item can no longer be claimed")
    void returnedItem_cannotBeClaimed() {
        Item item = buildItem("FOUND", "RETURNED");
        assertFalse(claimService.canClaim(item),
            "A RETURNED item should no longer accept new claims.");
    }

    // ── explanation text ──────────────────────────────────────────────────────

    /**
     * UT-13
     * "It has my initials on the back." is a genuine ownership detail —
     * exactly the kind of explanation the system wants to see.
     */
    @Test
    @DisplayName("UT-13 | a meaningful ownership explanation is accepted")
    void meaningfulExplanation_isAccepted() {
        boolean result = claimService.validateClaimExplanation(
            "It has my initials on the back.");
        assertTrue(result, "A non-blank explanation should return true.");
    }

    /**
     * UT-14
     * An empty explanation gives the reviewer nothing to work with.
     * The service must reject it right away.
     */
    @Test
    @DisplayName("UT-14 | an empty explanation is rejected — it is required")
    void emptyExplanation_isRejected() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> claimService.validateClaimExplanation(""),
            "An empty explanation should throw IllegalArgumentException."
        );
        assertNotNull(ex.getMessage(),
            "The exception should carry a message explaining why it was rejected.");
    }

    @Test
    @DisplayName("UT-14b | a blank (spaces-only) explanation is also rejected")
    void blankExplanation_isRejected() {
        assertThrows(
            IllegalArgumentException.class,
            () -> claimService.validateClaimExplanation("   "),
            "A whitespace-only explanation should also be rejected."
        );
    }

    // ── duplicate claim guard ─────────────────────────────────────────────────

    /**
     * UT-17
     * Student already has an ACTIVE claim on this exact item.
     * canSubmitDuplicateClaim should return false to signal the block.
     */
    @Test
    @DisplayName("UT-17 | a student with an active claim cannot submit another one")
    void studentWithActiveClaim_isBlockedFromDuplicate() {
        Long studentId = 2L;
        Long itemId    = 10L;

        // Build the existing active claim that is already in the database
        Claim activeClaim = new Claim(
            1L,          // claimId
            itemId,
            studentId,
            "The mouse has a small blue sticker with my initials underneath.",
            "ACTIVE",
            LocalDate.of(2026, 11, 3)
        );

        List<Claim> existingClaims = List.of(activeClaim);

        boolean canSubmit = claimService.canSubmitDuplicateClaim(existingClaims, studentId);
        assertFalse(canSubmit,
            "A student who already has an ACTIVE claim should be blocked from submitting again.");
    }

    @Test
    @DisplayName("UT-17b | a different student can still claim the same item")
    void differentStudent_canClaimSameItem() {
        Long originalStudentId = 2L;
        Long newStudentId      = 3L;
        Long itemId            = 10L;

        Claim activeClaim = new Claim(
            1L,
            itemId,
            originalStudentId,   // Student 2 already claimed it
            "The mouse has my initials.",
            "ACTIVE",
            LocalDate.of(2026, 11, 3)
        );

        List<Claim> existingClaims = List.of(activeClaim);

        // Student 3 is a different person — they should be able to claim
        boolean canSubmit = claimService.canSubmitDuplicateClaim(existingClaims, newStudentId);
        assertTrue(canSubmit,
            "A student who has NOT yet claimed this item should be allowed to submit.");
    }

    @Test
    @DisplayName("UT-17c | when there are no existing claims, any student may claim")
    void noExistingClaims_anyStudentCanClaim() {
        boolean canSubmit = claimService.canSubmitDuplicateClaim(
            Collections.emptyList(), 2L);
        assertTrue(canSubmit,
            "When no prior claims exist, the student should be free to submit one.");
    }

    // ── helper ────────────────────────────────────────────────────────────────

    /** Builds a minimal Item with the given type and status for readability. */
    private Item buildItem(String reportType, String status) {
        Item item = new Item();
        item.setItemId(10L);
        item.setReportType(reportType);
        item.setStatus(status);
        item.setCreatorUserId(1L);
        return item;
    }
}
