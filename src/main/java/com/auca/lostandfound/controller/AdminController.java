package com.auca.lostandfound.controller;

import com.auca.lostandfound.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Administrator-only moderation endpoints.
 * Access is controlled by the security filter — only requests that
 * carry X-User-Role: ADMIN are allowed through to these handlers.
 *
 * DELETE /api/admin/items/{id}  — IT-14, IT-15
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ItemService itemService;

    public AdminController(ItemService itemService) {
        this.itemService = itemService;
    }

    // ── DELETE /api/admin/items/{id} ──────────────────────────────────────────

    /**
     * IT-15: an administrator removes an inappropriate or duplicate report.
     * Returns 200 OK with {itemId, deleted: true} on success.
     * IT-14: a non-admin reaching this endpoint receives 403 — enforced by
     * SecurityConfig, so this handler only runs for authenticated admins.
     */
    @DeleteMapping("/items/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> {
                    itemService.deleteById(id);
                    return ResponseEntity.ok((Object)
                            Map.of("itemId", id, "deleted", true));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Item not found: " + id)));
    }
}
