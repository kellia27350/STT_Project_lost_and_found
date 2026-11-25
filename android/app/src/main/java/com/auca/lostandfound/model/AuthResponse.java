package com.auca.lostandfound.model;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {

    @SerializedName("token")
    private String token;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("email")
    private String email;

    @SerializedName("role")
    private String role;    // "STUDENT" or "ADMIN"

    public AuthResponse() {}

    public String getToken()  { return token; }
    public Long   getUserId() { return userId; }
    public String getEmail()  { return email; }
    public String getRole()   { return role; }
}
