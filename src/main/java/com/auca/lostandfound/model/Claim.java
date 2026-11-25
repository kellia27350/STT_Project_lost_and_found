package com.auca.lostandfound.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * A student's ownership claim on a Found item.
 *
 * claimStatus is "ACTIVE" while the claim is pending review,
 * and "RESOLVED" once the handover is confirmed or the item is returned.
 */
@Entity
@Table(name = "claims")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long claimId;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private Long claimantUserId;

    @Column(nullable = false, length = 1000)
    private String explanation;    // "It has my initials on the back."

    @Column(nullable = false)
    private String claimStatus;    // "ACTIVE" or "RESOLVED"

    private LocalDate submittedDate;

    // ── constructors ──────────────────────────────────────────────────────────

    public Claim() {}

    public Claim(Long claimId, Long itemId, Long claimantUserId,
                 String explanation, String claimStatus, LocalDate submittedDate) {
        this.claimId        = claimId;
        this.itemId         = itemId;
        this.claimantUserId = claimantUserId;
        this.explanation    = explanation;
        this.claimStatus    = claimStatus;
        this.submittedDate  = submittedDate;
    }

    // ── convenience helpers ───────────────────────────────────────────────────

    public boolean isActive()   { return "ACTIVE".equals(claimStatus); }
    public boolean isResolved() { return "RESOLVED".equals(claimStatus); }

    // ── getters / setters ─────────────────────────────────────────────────────

    public Long getClaimId()                           { return claimId; }
    public void setClaimId(Long claimId)               { this.claimId = claimId; }

    public Long getItemId()                            { return itemId; }
    public void setItemId(Long itemId)                 { this.itemId = itemId; }

    public Long getClaimantUserId()                            { return claimantUserId; }
    public void setClaimantUserId(Long claimantUserId)         { this.claimantUserId = claimantUserId; }

    public String getExplanation()                     { return explanation; }
    public void setExplanation(String explanation)     { this.explanation = explanation; }

    public String getClaimStatus()                     { return claimStatus; }
    public void setClaimStatus(String claimStatus)     { this.claimStatus = claimStatus; }

    public LocalDate getSubmittedDate()                        { return submittedDate; }
    public void setSubmittedDate(LocalDate submittedDate)      { this.submittedDate = submittedDate; }
}
