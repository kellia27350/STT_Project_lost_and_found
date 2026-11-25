package com.auca.lostandfound.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload sent by the Android client to POST /api/claims/item/{itemId}.
 * The student must supply written evidence of ownership.
 */
public class ClaimRequest {

    @NotBlank(message = "An ownership explanation is required when submitting a claim.")
    private String explanation;

    public ClaimRequest() {}

    public String getExplanation()                     { return explanation; }
    public void setExplanation(String explanation)     { this.explanation = explanation; }
}
