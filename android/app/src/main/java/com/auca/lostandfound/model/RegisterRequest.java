package com.auca.lostandfound.model;

public class RegisterRequest {
    private String studentId;
    private String name;
    private String email;
    private String password;

    public RegisterRequest(String studentId, String name, String email, String password) {
        this.studentId = studentId;
        this.name      = name;
        this.email     = email;
        this.password  = password;
    }

    public String getStudentId() { return studentId; }
    public String getName()      { return name; }
    public String getEmail()     { return email; }
    public String getPassword()  { return password; }
}
