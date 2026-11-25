package com.auca.lostandfound.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload for PATCH /api/items/{id}/status.
 * Carries the new status string ("AVAILABLE" or "RETURNED").
 */
public class StatusUpdateRequest {

    @NotBlank(message = "Status is required.")
    private String status;

    public StatusUpdateRequest() {}

    public String getStatus()              { return status; }
    public void setStatus(String status)   { this.status = status; }
}
