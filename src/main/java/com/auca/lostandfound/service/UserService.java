package com.auca.lostandfound.service;

import com.auca.lostandfound.dto.RegisterRequest;
import com.auca.lostandfound.dto.RegisterResponse;
import com.auca.lostandfound.model.User;
import com.auca.lostandfound.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Handles user account creation and credential validation.
 *
 * Pure validation helpers (validateEmail, validatePassword, validateStudentId)
 * are kept as plain methods so the existing unit tests (UT-01 to UT-05, UT-15)
 * can instantiate this class with {@code new UserService()} — they never call
 * the Spring-wired methods that need a repository.
 */
@Service
public class UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Primary constructor — Spring uses this when both dependencies are
     * available in the application context.
     */
    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * No-arg constructor kept so unit tests can call {@code new UserService()}
     * without wiring a Spring context.
     */
    public UserService() {
        this.userRepository  = null;
        this.passwordEncoder = null;
    }

    // ── email ─────────────────────────────────────────────────────────────────

    /**
     * Returns true when the address is non-blank and contains an '@' symbol.
     *
     * UT-01  kellia@example.com  → true
     * UT-02  kelliaexample.com   → exception (missing '@')
     * UT-03  ""                  → exception (empty)
     */
    public boolean validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException(
                "'" + email + "' is not a valid email — the '@' symbol is missing.");
        }
        return true;
    }

    // ── password ──────────────────────────────────────────────────────────────

    /**
     * A password must be at least 8 characters long.
     *
     * UT-04  Pass@2026  → true
     * UT-05  abc        → exception (too short)
     */
    public boolean validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException(
                "Password must be at least 8 characters long.");
        }
        return true;
    }

    // ── student ID ────────────────────────────────────────────────────────────

    /**
     * Confirms that a student ID was actually provided.
     *
     * UT-15  null / ""  → exception
     */
    public boolean validateStudentId(String studentId) {
        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("Student ID is required.");
        }
        return true;
    }

    // ── registration ──────────────────────────────────────────────────────────

    /**
     * Validates the payload, checks for duplicate email, hashes the password,
     * saves the new account, and returns a RegisterResponse (never the hash).
     *
     * IT-01: returns saved user data with no password field
     * IT-02: throws if the email is already taken
     */
    public RegisterResponse register(RegisterRequest req) {
        validateEmail(req.getEmail());
        validatePassword(req.getPassword());
        validateStudentId(req.getStudentId());

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalStateException(
                "An account with that email address already exists.");
        }

        User user = new User();
        user.setStudentId(req.getStudentId());
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(User.Role.STUDENT);

        User saved = userRepository.save(user);

        return new RegisterResponse(
            saved.getUserId(),
            saved.getStudentId(),
            saved.getName(),
            saved.getEmail(),
            saved.getRole().name()
        );
    }

    // ── credential lookup ─────────────────────────────────────────────────────

    /**
     * Finds a user by email; returns empty if not found.
     * The controller uses this together with passwordEncoder.matches()
     * to authenticate login requests.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
