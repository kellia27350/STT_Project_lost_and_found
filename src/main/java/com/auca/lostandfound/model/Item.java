package com.auca.lostandfound.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Represents a single lost or found item report posted by a student.
 *
 * reportType is always either "LOST" or "FOUND".
 * status moves from "AVAILABLE" to "RETURNED" once the owner
 * confirms physical handover with the finder.
 */
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String location;

    private LocalDate reportDate;

    @Column(nullable = false)
    private String reportType;   // "LOST" or "FOUND"

    @Column(nullable = false)
    private String status;       // "AVAILABLE" or "RETURNED"

    @Column(nullable = false)
    private Long creatorUserId;

    // ── constructors ──────────────────────────────────────────────────────────

    public Item() {}

    public Item(Long itemId, String name, String category, String description,
                String location, LocalDate reportDate,
                String reportType, String status, Long creatorUserId) {
        this.itemId        = itemId;
        this.name          = name;
        this.category      = category;
        this.description   = description;
        this.location      = location;
        this.reportDate    = reportDate;
        this.reportType    = reportType;
        this.status        = status;
        this.creatorUserId = creatorUserId;
    }

    // ── convenience helpers ───────────────────────────────────────────────────

    public boolean isFound()     { return "FOUND".equals(reportType); }
    public boolean isReturned()  { return "RETURNED".equals(status); }
    public boolean isAvailable() { return "AVAILABLE".equals(status); }

    // ── getters / setters ─────────────────────────────────────────────────────

    public Long getItemId()                        { return itemId; }
    public void setItemId(Long itemId)             { this.itemId = itemId; }

    public String getName()                        { return name; }
    public void setName(String name)               { this.name = name; }

    public String getCategory()                    { return category; }
    public void setCategory(String category)       { this.category = category; }

    public String getDescription()                 { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation()                    { return location; }
    public void setLocation(String location)       { this.location = location; }

    public LocalDate getReportDate()                       { return reportDate; }
    public void setReportDate(LocalDate reportDate)        { this.reportDate = reportDate; }

    public String getReportType()                          { return reportType; }
    public void setReportType(String reportType)           { this.reportType = reportType; }

    public String getStatus()                      { return status; }
    public void setStatus(String status)           { this.status = status; }

    public Long getCreatorUserId()                         { return creatorUserId; }
    public void setCreatorUserId(Long creatorUserId)       { this.creatorUserId = creatorUserId; }
}
