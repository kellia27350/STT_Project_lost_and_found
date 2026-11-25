package com.auca.lostandfound.service;

import com.auca.lostandfound.model.Claim;
import com.auca.lostandfound.model.Item;
import com.auca.lostandfound.repository.ClaimRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Guards the rules around ownership claims and persists them.
 *
 * The rule-checking helpers (canClaim, validateClaimExplanation,
 * canSubmitDuplicateClaim) remain as plain methods so the unit tests
 * (UT-10 to UT-14, UT-17) can call them via {@code new ClaimService()}.
 */
@Service
public class ClaimService {

    private final ClaimRepository claimRepository;

    /** Spring constructor injection. */
    @Autowired
    public ClaimService(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    /** No-arg constructor for unit tests that don't need persistence. */
    public ClaimService() {
        this.claimRepository = null;
    }

    // ── can a claim be opened? ────────────────────────────────────────────────

    /**
     * UT-10  FOUND + AVAILABLE   → true
     * UT-11  LOST  + AVAILABLE   → false
     * UT-12  FOUND + RETURNED    → false
     */
    public boolean canClaim(Item item) {
        if (item == null) return false;
        return item.isFound() && item.isAvailable();
    }

    // ── explanation text ──────────────────────────────────────────────────────

    /**
     * UT-13  "It has my initials on the back."  → true
     * UT-14  ""                                 → exception
     */
    public boolean validateClaimExplanation(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                "An ownership explanation is required when submitting a claim.");
        }
        return true;
    }

    // ── duplicate claim guard ─────────────────────────────────────────────────

    /**
     * UT-17  Student already has an ACTIVE claim on item → false (block)
     */
    public boolean canSubmitDuplicateClaim(List<Claim> existingClaims, Long userId) {
        if (existingClaims == null || existingClaims.isEmpty()) return true;

        boolean alreadyHasActiveClaim = existingClaims.stream()
                .anyMatch(c -> c.getClaimantUserId().equals(userId) && c.isActive());

        return !alreadyHasActiveClaim;
    }

    // ── submit claim ──────────────────────────────────────────────────────────

    /**
     * IT-08: validates rules, persists, and returns the saved Claim.
     */
    public Claim submitClaim(Item item, Long claimantUserId, String explanation) {
        validateClaimExplanation(explanation);

        if (!canClaim(item)) {
            throw new IllegalStateException(
                "Claims can only be opened on Found items that are still available.");
        }

        // Duplicate check using the live repository
        List<Claim> existing = claimRepository.findByItemId(item.getItemId());
        if (!canSubmitDuplicateClaim(existing, claimantUserId)) {
            throw new IllegalStateException(
                "You already have an active claim on this item.");
        }

        Claim claim = new Claim();
        claim.setItemId(item.getItemId());
        claim.setClaimantUserId(claimantUserId);
        claim.setExplanation(explanation);
        claim.setClaimStatus("ACTIVE");
        claim.setSubmittedDate(LocalDate.now());

        return claimRepository.save(claim);
    }

    public List<Claim> findByItemId(Long itemId) {
        return claimRepository.findByItemId(itemId);
    }
}
