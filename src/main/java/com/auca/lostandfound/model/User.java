package com.auca.lostandfound.model;

import jakarta.persistence.*;

/**
 * Represents a person who uses the Lost and Found system.
 * A user can be a regular student or a campus administrator.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String studentId;   // e.g. "27350"

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public enum Role {
        STUDENT,
        ADMIN
    }

    // ── constructors ──────────────────────────────────────────────────────────

    public User() {}

    public User(Long userId, String studentId, String name, String email,
                String passwordHash, Role role) {
        this.userId       = userId;
        this.studentId    = studentId;
        this.name         = name;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.role         = role;
    }

    // ── getters / setters ─────────────────────────────────────────────────────

    public Long getUserId()                    { return userId; }
    public void setUserId(Long userId)         { this.userId = userId; }

    public String getStudentId()               { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getName()                    { return name; }
    public void setName(String name)           { this.name = name; }

    public String getEmail()                   { return email; }
    public void setEmail(String email)         { this.email = email; }

    public String getPasswordHash()                      { return passwordHash; }
    public void setPasswordHash(String passwordHash)     { this.passwordHash = passwordHash; }

    public Role getRole()                      { return role; }
    public void setRole(Role role)             { this.role = role; }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
