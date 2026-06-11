package com.example.yamlservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standardised error response body.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private int status;
    private String error;
    private String message;
    private String path;
    private Instant timestamp;
    private List<String> details;

    public ApiError() {
        this.timestamp = Instant.now();
    }

    public ApiError(int status, String error, String message, String path) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public List<String> getDetails() { return details; }
    public void setDetails(List<String> details) { this.details = details; }
}
