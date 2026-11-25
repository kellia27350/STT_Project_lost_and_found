package com.auca.lostandfound.dto;

/**
 * Data sent by the Android client when a student submits a new item report.
 * This is a plain data carrier — no logic lives here.
 */
public class ItemRequest {

    private String name;
    private String category;
    private String description;
    private String location;
    private String reportDate;   // ISO-8601 string, e.g. "2026-11-03"
    private String reportType;   // "LOST" or "FOUND"

    // ── constructors ──────────────────────────────────────────────────────────

    public ItemRequest() {}

    public ItemRequest(String name, String category, String description,
                       String location, String reportDate, String reportType) {
        this.name        = name;
        this.category    = category;
        this.description = description;
        this.location    = location;
        this.reportDate  = reportDate;
        this.reportType  = reportType;
    }

    // ── getters / setters ─────────────────────────────────────────────────────

    public String getName()                        { return name; }
    public void setName(String name)               { this.name = name; }

    public String getCategory()                    { return category; }
    public void setCategory(String category)       { this.category = category; }

    public String getDescription()                 { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation()                    { return location; }
    public void setLocation(String location)       { this.location = location; }

    public String getReportDate()                          { return reportDate; }
    public void setReportDate(String reportDate)           { this.reportDate = reportDate; }

    public String getReportType()                          { return reportType; }
    public void setReportType(String reportType)           { this.reportType = reportType; }
}
