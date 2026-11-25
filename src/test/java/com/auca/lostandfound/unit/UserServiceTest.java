package com.auca.lostandfound.unit;

import com.auca.lostandfound.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserService.
 *
 * Covers UT-01 through UT-06 and UT-15:
 *   email validation, password validation, and student ID validation.
 *
 * No database or Spring context is needed here — UserService is
 * instantiated directly with `new` and all logic runs in memory.
 */
@DisplayName("UserService — validation rules")
class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        // Fresh instance before every test so state never leaks between cases.
        userService = new UserService();
    }

    // ── email ─────────────────────────────────────────────────────────────────

    /**
     * UT-01
     * A properly formed email address should sail straight through.
     */
    @Test
    @DisplayName("UT-01 | valid email like kellia@example.com is accepted")
    void validEmail_isAccepted() {
        boolean result = userService.validateEmail("kellia@example.com");
        assertTrue(result, "A properly formed email should return true.");
    }

    /**
     * UT-02
     * Without the '@' symbol the string is not an email at all.
     * The service must reject it with a clear message.
     */
    @Test
    @DisplayName("UT-02 | email without '@' is rejected")
    void emailMissingAtSign_isRejected() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> userService.validateEmail("kelliaexample.com"),
            "An email missing '@' should throw IllegalArgumentException."
        );
        assertTrue(ex.getMessage().contains("@"),
            "The error message should mention the missing '@' symbol.");
    }

    /**
     * UT-03
     * A completely empty string is not a valid email.
     * Required fields can't be satisfied by nothing.
     */
    @Test
    @DisplayName("UT-03 | empty email string is rejected as a required field")
    void emptyEmail_isRejected() {
        assertThrows(
            IllegalArgumentException.class,
            () -> userService.validateEmail(""),
            "An empty email should throw IllegalArgumentException."
        );
    }

    // ── password ──────────────────────────────────────────────────────────────

    /**
     * UT-04
     * "Pass@2026" is 9 characters and meets the minimum-length rule.
     */
    @Test
    @DisplayName("UT-04 | a strong password like 'Pass@2026' is accepted")
    void strongPassword_isAccepted() {
        boolean result = userService.validatePassword("Pass@2026");
        assertTrue(result, "A password meeting the length rule should return true.");
    }

    /**
     * UT-05
     * "abc" is only 3 characters — well below the 8-character minimum.
     * The service should tell the student it is too short.
     */
    @Test
    @DisplayName("UT-05 | a short password like 'abc' is rejected")
    void tooShortPassword_isRejected() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> userService.validatePassword("abc"),
            "A password shorter than 8 characters should throw IllegalArgumentException."
        );
        assertTrue(ex.getMessage().toLowerCase().contains("8"),
            "The error message should mention the 8-character minimum.");
    }

    // ── student ID ────────────────────────────────────────────────────────────

    /**
     * UT-15
     * A student ID of null or blank is meaningless.
     * The system cannot link an account to anyone without it.
     */
    @Test
    @DisplayName("UT-15a | null student ID is rejected as mandatory")
    void nullStudentId_isRejected() {
        assertThrows(
            IllegalArgumentException.class,
            () -> userService.validateStudentId(null),
            "A null student ID should throw IllegalArgumentException."
        );
    }

    @Test
    @DisplayName("UT-15b | empty student ID is rejected as mandatory")
    void emptyStudentId_isRejected() {
        assertThrows(
            IllegalArgumentException.class,
            () -> userService.validateStudentId(""),
            "An empty student ID should throw IllegalArgumentException."
        );
    }

    @Test
    @DisplayName("UT-15c | a real student ID like '27350' is accepted")
    void validStudentId_isAccepted() {
        boolean result = userService.validateStudentId("27350");
        assertTrue(result, "A non-blank student ID should return true.");
    }
}
