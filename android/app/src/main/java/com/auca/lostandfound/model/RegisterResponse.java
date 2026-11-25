package com.auca.lostandfound.model;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse {

    @SerializedName("userId")
    private Long userId;

    @SerializedName("studentId")
    private String studentId;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;

    public RegisterResponse() {}

    public Long   getUserId()    { return userId; }
    public String getStudentId() { return studentId; }
    public String getName()      { return name; }
    public String getEmail()     { return email; }
    public String getRole()      { return role; }
}
