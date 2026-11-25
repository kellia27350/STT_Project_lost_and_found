package com.auca.lostandfound.unit;

import com.auca.lostandfound.dto.ItemRequest;
import com.auca.lostandfound.model.Item;
import com.auca.lostandfound.model.User;
import com.auca.lostandfound.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ItemService.
 *
 * Covers UT-06 through UT-09, UT-16, and UT-18:
 *   item name, report type, required-fields check, and modification rights.
 *
 * Everything runs in memory — no database, no Spring context.
 */
@DisplayName("ItemService — validation and ownership rules")
class ItemServiceTest {

    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemService();
    }

    // ── item name ─────────────────────────────────────────────────────────────

    /**
     * UT-06
     * A descriptive item name like "Wireless Mouse" is exactly what
     * the system expects and should be accepted without complaint.
     */
    @Test
    @DisplayName("UT-06 | a real item name like 'Wireless Mouse' is accepted")
    void normalItemName_isAccepted() {
        boolean result = itemService.validateItemName("Wireless Mouse");
        assertTrue(result, "A non-blank item name should return true.");
    }

    /**
     * UT-07
     * A string made entirely of spaces looks non-empty but carries
     * no information — the service treats it as blank and rejects it.
     */
    @Test
    @DisplayName("UT-07 | a whitespace-only item name is rejected")
    void whitespaceItemName_isRejected() {
        assertThrows(
            IllegalArgumentException.class,
            () -> itemService.validateItemName("   "),
            "A whitespace-only name should throw IllegalArgumentException."
        );
    }

    /**
     * UT-07b
     * A completely empty string is equally meaningless.
     */
    @Test
    @DisplayName("UT-07b | an empty item name is rejected")
    void emptyItemName_isRejected() {
        assertThrows(
            IllegalArgumentException.class,
            () -> itemService.validateItemName(""),
            "An empty item name should throw IllegalArgumentException."
        );
    }

    // ── report type ───────────────────────────────────────────────────────────

    /**
     * UT-08
     * "FOUND" is one of the two valid report types and must pass cleanly.
     */
    @Test
    @DisplayName("UT-08 | 'FOUND' is a valid report type")
    void foundReportType_isAccepted() {
        boolean result = itemService.validateReportType("FOUND");
        assertTrue(result, "'FOUND' should be a valid report type.");
    }

    /**
     * UT-08b
     * Lower-case "lost" should also work — the service normalises it.
     */
    @Test
    @DisplayName("UT-08b | 'lost' (lowercase) is also accepted as 'LOST'")
    void lowercaseLostReportType_isAccepted() {
        boolean result = itemService.validateReportType("lost");
        assertTrue(result, "Lowercase 'lost' should be normalised to LOST and accepted.");
    }

    /**
     * UT-09
     * "MISSING" sounds reasonable but is not one of the two accepted values.
     * The service must reject it and hint at the correct options.
     */
    @Test
    @DisplayName("UT-09 | 'MISSING' is not a valid report type and is rejected")
    void invalidReportType_isRejected() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> itemService.validateReportType("MISSING"),
            "'MISSING' should throw IllegalArgumentException."
        );
        assertTrue(ex.getMessage().contains("LOST") || ex.getMessage().contains("FOUND"),
            "The error message should tell the student which values are allowed.");
    }

    // ── required fields ───────────────────────────────────────────────────────

    /**
     * UT-16
     * Even when name and type are correct, missing location or date
     * is enough to have the whole report rejected.
     */
    @Test
    @DisplayName("UT-16a | a report with valid name/type but no location is rejected")
    void missingLocation_reportIsRejected() {
        ItemRequest req = new ItemRequest(
            "Blue Notebook",   // name        ✓
            "Books",           // category    ✓
            "A5 spiral notebook with blue cover", // description ✓
            "",                // location    ✗  — missing
            "2026-11-02",      // date        ✓
            "LOST"             // type        ✓
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> itemService.validateRequiredFields(req),
            "A missing location should throw IllegalArgumentException."
        );
    }

    @Test
    @DisplayName("UT-16b | a report with valid name/type but no date is rejected")
    void missingDate_reportIsRejected() {
        ItemRequest req = new ItemRequest(
            "Blue Notebook",
            "Books",
            "A5 spiral notebook",
            "Classroom B12",
            "",                // date  ✗ — missing
            "LOST"
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> itemService.validateRequiredFields(req),
            "A missing report date should throw IllegalArgumentException."
        );
    }

    @Test
    @DisplayName("UT-16c | a fully complete report passes validation")
    void completeReport_passesValidation() {
        ItemRequest req = new ItemRequest(
            "Blue Notebook",
            "Books",
            "A5 spiral notebook with blue cover",
            "Classroom B12",
            "2026-11-02",
            "LOST"
        );
        assertTrue(itemService.validateRequiredFields(req),
            "A complete, valid request should return true.");
    }

    // ── modification rights ───────────────────────────────────────────────────

    /**
     * UT-18
     * Student B should not be able to edit or delete a report that
     * Student A created.  Only the owner or an admin may do so.
     */
    @Test
    @DisplayName("UT-18a | a different student cannot modify another student's report")
    void studentB_cannotModifyStudentAsReport() {
        // Student A owns the item (userId = 1)
        Item item = new Item();
        item.setItemId(10L);
        item.setCreatorUserId(1L);

        // Student B tries to edit it (userId = 2)
        User studentB = new User();
        studentB.setUserId(2L);
        studentB.setRole(User.Role.STUDENT);

        boolean allowed = itemService.canModifyReport(item, studentB);
        assertFalse(allowed,
            "A student who did not create the report should not be allowed to modify it.");
    }

    @Test
    @DisplayName("UT-18b | the report owner can always modify their own report")
    void reportOwner_canModifyOwnReport() {
        Item item = new Item();
        item.setItemId(10L);
        item.setCreatorUserId(1L);

        User owner = new User();
        owner.setUserId(1L);
        owner.setRole(User.Role.STUDENT);

        assertTrue(itemService.canModifyReport(item, owner),
            "The student who created the report should be allowed to modify it.");
    }

    @Test
    @DisplayName("UT-18c | an administrator can modify any report on campus")
    void admin_canModifyAnyReport() {
        Item item = new Item();
        item.setItemId(10L);
        item.setCreatorUserId(1L);   // owned by Student A

        User admin = new User();
        admin.setUserId(99L);        // different user
        admin.setRole(User.Role.ADMIN);

        assertTrue(itemService.canModifyReport(item, admin),
            "An administrator should be able to modify any report.");
    }
}
