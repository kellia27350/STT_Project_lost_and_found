package com.auca.lostandfound.controller;

import com.auca.lostandfound.dto.ItemRequest;
import com.auca.lostandfound.dto.StatusUpdateRequest;
import com.auca.lostandfound.model.Item;
import com.auca.lostandfound.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Item report management endpoints.
 *
 * POST   /api/items                  — IT-05, IT-06
 * GET    /api/items?type=&category=  — IT-11
 * GET    /api/items/search?keyword=  — IT-07, IT-12
 * PATCH  /api/items/{id}/status      — IT-10
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // ── POST /api/items ───────────────────────────────────────────────────────

    /**
     * IT-05 / IT-06
     * The authenticated user's id is passed in the X-User-Id header by the
     * security filter and re-forwarded by the Android client.
     */
    @PostMapping
    public ResponseEntity<?> createReport(
            @Valid @RequestBody ItemRequest req,
            @RequestHeader("X-User-Id") Long userId) {
        try {
            Item saved = itemService.createReport(req, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET /api/items ────────────────────────────────────────────────────────

    /**
     * IT-11: optional ?type= and ?category= query parameters narrow results.
     * Returns all items when both parameters are absent.
     */
    @GetMapping
    public ResponseEntity<List<Item>> listItems(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(itemService.findFiltered(type, category));
    }

    // ── GET /api/items/search ─────────────────────────────────────────────────

    /**
     * IT-07: keyword search across name, description, and location.
     * IT-12: returns an empty array — not an error — when nothing matches.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Item>> search(
            @RequestParam String keyword) {
        return ResponseEntity.ok(itemService.searchByKeyword(keyword));
    }

    // ── PATCH /api/items/{id}/status ──────────────────────────────────────────

    /**
     * IT-10: updates an item's status (e.g. AVAILABLE → RETURNED).
     * Only the report creator or an admin may call this; role enforcement is
     * handled by the security filter via X-User-Id / X-User-Role headers.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest req) {
        return itemService.findById(id)
                .map(item -> {
                    item.setStatus(req.getStatus().toUpperCase());
                    Item saved = itemService.save(item);
                    return ResponseEntity.ok((Object) saved);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Item not found: " + id)));
    }
}
