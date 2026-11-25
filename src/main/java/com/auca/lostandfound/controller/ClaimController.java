package com.auca.lostandfound.controller;

import com.auca.lostandfound.dto.ClaimRequest;
import com.auca.lostandfound.model.Claim;
import com.auca.lostandfound.model.Item;
import com.auca.lostandfound.service.ClaimService;
import com.auca.lostandfound.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Claim submission endpoint.
 *
 * POST /api/claims/item/{itemId}  — IT-08, IT-09, IT-13
 */
@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;
    private final ItemService  itemService;

    public ClaimController(ClaimService claimService, ItemService itemService) {
        this.claimService = claimService;
        this.itemService  = itemService;
    }

    // ── POST /api/claims/item/{itemId} ────────────────────────────────────────

    /**
     * IT-08: saves a valid claim (ACTIVE, linked to item and claimant).
     * IT-09: returns 409 when the item is already RETURNED.
     * IT-13: returns 409 when the same student already has an ACTIVE claim.
     *
     * The authenticated student's id is forwarded by the security filter
     * in the X-User-Id header.
     */
    @PostMapping("/item/{itemId}")
    public ResponseEntity<?> submitClaim(
            @PathVariable Long itemId,
            @Valid @RequestBody ClaimRequest req,
            @RequestHeader("X-User-Id") Long userId) {

        Optional<Item> maybeItem = itemService.findById(itemId);
        if (maybeItem.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Item not found: " + itemId));
        }

        try {
            Claim saved = claimService.submitClaim(
                    maybeItem.get(), userId, req.getExplanation());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
