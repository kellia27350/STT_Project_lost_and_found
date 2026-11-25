package com.auca.lostandfound.service;

import com.auca.lostandfound.dto.ItemRequest;
import com.auca.lostandfound.model.Item;
import com.auca.lostandfound.model.User;
import com.auca.lostandfound.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Enforces business rules for item reports and delegates persistence
 * to ItemRepository.
 *
 * The validation helpers (validateItemName, validateReportType,
 * validateRequiredFields, canModifyReport) are kept as plain methods
 * so unit tests can call them with {@code new ItemService()}.
 */
@Service
public class ItemService {

    private static final Set<String> ALLOWED_TYPES = Set.of("LOST", "FOUND");

    private final ItemRepository itemRepository;

    /** Spring constructor injection. */
    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /** No-arg constructor for unit tests that don't need persistence. */
    public ItemService() {
        this.itemRepository = null;
    }

    // ── item name ─────────────────────────────────────────────────────────────

    /**
     * UT-06  "Wireless Mouse"  → true
     * UT-07  "   "             → exception
     */
    public boolean validateItemName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Item name cannot be blank.");
        }
        return true;
    }

    // ── report type ───────────────────────────────────────────────────────────

    /**
     * UT-08  "FOUND"    → true
     * UT-09  "MISSING"  → exception
     */
    public boolean validateReportType(String type) {
        if (type == null || !ALLOWED_TYPES.contains(type.toUpperCase())) {
            throw new IllegalArgumentException(
                "Report type must be LOST or FOUND, not '" + type + "'.");
        }
        return true;
    }

    // ── required fields ───────────────────────────────────────────────────────

    /**
     * UT-16  missing location or date → exception
     */
    public boolean validateRequiredFields(ItemRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Item request cannot be null.");
        }
        validateItemName(req.getName());
        validateReportType(req.getReportType());

        if (req.getCategory() == null || req.getCategory().isBlank()) {
            throw new IllegalArgumentException("Category is required.");
        }
        if (req.getDescription() == null || req.getDescription().isBlank()) {
            throw new IllegalArgumentException("Description is required.");
        }
        if (req.getLocation() == null || req.getLocation().isBlank()) {
            throw new IllegalArgumentException("Location is required.");
        }
        if (req.getReportDate() == null || req.getReportDate().isBlank()) {
            throw new IllegalArgumentException("Report date is required.");
        }
        return true;
    }

    // ── ownership check ───────────────────────────────────────────────────────

    /**
     * UT-18  only the report creator or an admin may modify the item.
     */
    public boolean canModifyReport(Item item, User user) {
        if (item == null || user == null) return false;
        return item.getCreatorUserId().equals(user.getUserId()) || user.isAdmin();
    }

    // ── create report ─────────────────────────────────────────────────────────

    /**
     * IT-05 / IT-06: validates, persists, and returns the saved Item.
     */
    public Item createReport(ItemRequest req, Long creatorUserId) {
        validateRequiredFields(req);

        Item item = new Item();
        item.setName(req.getName());
        item.setCategory(req.getCategory());
        item.setDescription(req.getDescription());
        item.setLocation(req.getLocation());
        item.setReportType(req.getReportType().toUpperCase());
        item.setStatus("AVAILABLE");
        item.setCreatorUserId(creatorUserId);

        try {
            item.setReportDate(LocalDate.parse(req.getReportDate()));
        } catch (Exception ignored) {
            item.setReportDate(LocalDate.now());
        }

        // Persist only when the repository is wired (integration / production)
        if (itemRepository != null) {
            return itemRepository.save(item);
        }
        return item;
    }

    // ── queries ───────────────────────────────────────────────────────────────

    public List<Item> searchByKeyword(String keyword) {
        return itemRepository.searchByKeyword(keyword);
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public List<Item> findFiltered(String type, String category) {
        if (type != null && !type.isBlank() && category != null && !category.isBlank()) {
            return itemRepository.findByReportTypeAndCategory(
                type.toUpperCase(), category);
        }
        if (type != null && !type.isBlank()) {
            return itemRepository.findByReportType(type.toUpperCase());
        }
        if (category != null && !category.isBlank()) {
            return itemRepository.findByCategory(category);
        }
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long itemId) {
        return itemRepository.findById(itemId);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long itemId) {
        itemRepository.deleteById(itemId);
    }
}
