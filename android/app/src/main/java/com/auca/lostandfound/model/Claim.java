package com.auca.lostandfound.model;

import com.google.gson.annotations.SerializedName;

/**
 * Mirrors the backend Claim entity returned by the REST API.
 */
public class Claim {

    @SerializedName("claimId")
    private Long claimId;

    @SerializedName("itemId")
    private Long itemId;

    @SerializedName("claimantUserId")
    private Long claimantUserId;

    @SerializedName("explanation")
    private String explanation;

    @SerializedName("claimStatus")
    private String claimStatus;    // "ACTIVE" or "RESOLVED"

    @SerializedName("submittedDate")
    private String submittedDate;

    public Claim() {}

    public boolean isActive()   { return "ACTIVE".equals(claimStatus); }

    public Long   getClaimId()                       { return claimId; }
    public void   setClaimId(Long claimId)           { this.claimId = claimId; }

    public Long   getItemId()                        { return itemId; }
    public void   setItemId(Long itemId)             { this.itemId = itemId; }

    public Long   getClaimantUserId()                { return claimantUserId; }
    public void   setClaimantUserId(Long id)         { this.claimantUserId = id; }

    public String getExplanation()                   { return explanation; }
    public void   setExplanation(String e)           { this.explanation = e; }

    public String getClaimStatus()                   { return claimStatus; }
    public void   setClaimStatus(String s)           { this.claimStatus = s; }

    public String getSubmittedDate()                 { return submittedDate; }
    public void   setSubmittedDate(String d)         { this.submittedDate = d; }
}
