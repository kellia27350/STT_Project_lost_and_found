package com.auca.lostandfound.dto;

/**
 * Response body for a successful registration (IT-01).
 * Returns the saved user data — never the password or its hash.
 */
public class RegisterResponse {

    private Long   userId;
    private String studentId;
    private String name;
    private String email;
    private String role;

    public RegisterResponse() {}

    public RegisterResponse(Long userId, String studentId,
                            String name, String email, String role) {
        this.userId    = userId;
        this.studentId = studentId;
        this.name      = name;
        this.email     = email;
        this.role      = role;
    }

    public Long getUserId()                    { return userId; }
    public void setUserId(Long userId)         { this.userId = userId; }

    public String getStudentId()               { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getName()                    { return name; }
    public void setName(String name)           { this.name = name; }

    public String getEmail()                   { return email; }
    public void setEmail(String email)         { this.email = email; }

    public String getRole()                    { return role; }
    public void setRole(String role)           { this.role = role; }
}
