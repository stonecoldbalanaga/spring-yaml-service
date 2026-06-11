package com.example.yamlservice.model;

/**
 * JWT token response payload.
 */
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private long expiresIn;

    public AuthResponse() {}

    public AuthResponse(String token, long expiresIn) {
        this.token = token;
        this.expiresIn = expiresIn;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
}
