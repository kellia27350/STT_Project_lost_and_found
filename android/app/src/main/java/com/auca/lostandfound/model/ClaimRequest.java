package com.auca.lostandfound.model;

public class ClaimRequest {
    private String explanation;

    public ClaimRequest(String explanation) {
        this.explanation = explanation;
    }

    public String getExplanation() { return explanation; }
}
