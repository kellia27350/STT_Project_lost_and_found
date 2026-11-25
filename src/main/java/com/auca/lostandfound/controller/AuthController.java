package com.auca.lostandfound.controller;

import com.auca.lostandfound.dto.AuthResponse;
import com.auca.lostandfound.dto.LoginRequest;
import com.auca.lostandfound.dto.RegisterRequest;
import com.auca.lostandfound.dto.RegisterResponse;
import com.auca.lostandfound.model.User;
import com.auca.lostandfound.security.JwtUtil;
import com.auca.lostandfound.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Handles student registration and login.
 *
 * POST /api/auth/register  — IT-01, IT-02
 * POST /api/auth/login     — IT-03, IT-04
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService     userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil         jwtUtil;

    public AuthController(UserService userService,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userService     = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil         = jwtUtil;
    }

    // ── POST /api/auth/register ───────────────────────────────────────────────

    /**
     * IT-01: returns 201 with the saved user (email, no password).
     * IT-02: returns 409 when the email is already registered.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        try {
            RegisterResponse response = userService.register(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            // duplicate email
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ── POST /api/auth/login ──────────────────────────────────────────────────

    /**
     * IT-03: returns 200 with a JWT token when credentials are correct.
     * IT-04: returns 401 when the password is wrong or the account does not exist.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        return userService.findByEmail(req.getEmail())
                .filter(user -> passwordEncoder.matches(req.getPassword(),
                                                        user.getPasswordHash()))
                .map(user -> {
                    String token = jwtUtil.generateToken(
                            user.getUserId(),
                            user.getEmail(),
                            user.getRole().name());
                    return ResponseEntity.ok((Object)
                            new AuthResponse(token, user.getUserId(),
                                             user.getEmail(), user.getRole().name()));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid email or password.")));
    }
}
