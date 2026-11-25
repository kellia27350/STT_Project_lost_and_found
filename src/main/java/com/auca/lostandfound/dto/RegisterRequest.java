package com.auca.lostandfound.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload sent by the Android client to POST /api/auth/register.
 */
public class RegisterRequest {

    @NotBlank(message = "Student ID is required.")
    private String studentId;

    @NotBlank(message = "Name is required.")
    private String name;

    @NotBlank(message = "Email is required.")
    @Email(message = "A valid email address is required.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    private String password;

    public RegisterRequest() {}

    public String getStudentId()               { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getName()                    { return name; }
    public void setName(String name)           { this.name = name; }

    public String getEmail()                   { return email; }
    public void setEmail(String email)         { this.email = email; }

    public String getPassword()                { return password; }
    public void setPassword(String password)   { this.password = password; }
}
