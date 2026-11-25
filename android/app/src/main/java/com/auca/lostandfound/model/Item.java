package com.auca.lostandfound.model;

import com.google.gson.annotations.SerializedName;

/**
 * Mirrors the backend Item entity returned by the REST API.
 */
public class Item {

    @SerializedName("itemId")
    private Long itemId;

    @SerializedName("name")
    private String name;

    @SerializedName("category")
    private String category;

    @SerializedName("description")
    private String description;

    @SerializedName("location")
    private String location;

    @SerializedName("reportDate")
    private String reportDate;      // ISO-8601 string "2026-11-03"

    @SerializedName("reportType")
    private String reportType;      // "LOST" or "FOUND"

    @SerializedName("status")
    private String status;          // "AVAILABLE" or "RETURNED"

    @SerializedName("creatorUserId")
    private Long creatorUserId;

    public Item() {}

    // ── convenience helpers ───────────────────────────────────────────────────

    public boolean isFound()     { return "FOUND".equals(reportType); }
    public boolean isAvailable() { return "AVAILABLE".equals(status); }
    public boolean isReturned()  { return "RETURNED".equals(status); }
    public boolean isClaimable() { return isFound() && isAvailable(); }

    // ── getters / setters ─────────────────────────────────────────────────────

    public Long   getItemId()                        { return itemId; }
    public void   setItemId(Long itemId)             { this.itemId = itemId; }

    public String getName()                          { return name; }
    public void   setName(String name)               { this.name = name; }

    public String getCategory()                      { return category; }
    public void   setCategory(String category)       { this.category = category; }

    public String getDescription()                   { return description; }
    public void   setDescription(String d)           { this.description = d; }

    public String getLocation()                      { return location; }
    public void   setLocation(String location)       { this.location = location; }

    public String getReportDate()                    { return reportDate; }
    public void   setReportDate(String reportDate)   { this.reportDate = reportDate; }

    public String getReportType()                    { return reportType; }
    public void   setReportType(String reportType)   { this.reportType = reportType; }

    public String getStatus()                        { return status; }
    public void   setStatus(String status)           { this.status = status; }

    public Long   getCreatorUserId()                 { return creatorUserId; }
    public void   setCreatorUserId(Long id)          { this.creatorUserId = id; }
}
