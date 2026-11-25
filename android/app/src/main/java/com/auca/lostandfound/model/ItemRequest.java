package com.auca.lostandfound.model;

public class ItemRequest {
    private String name;
    private String category;
    private String description;
    private String location;
    private String reportDate;   // "2026-11-03"
    private String reportType;   // "LOST" or "FOUND"

    public ItemRequest(String name, String category, String description,
                       String location, String reportDate, String reportType) {
        this.name        = name;
        this.category    = category;
        this.description = description;
        this.location    = location;
        this.reportDate  = reportDate;
        this.reportType  = reportType;
    }

    public String getName()        { return name; }
    public String getCategory()    { return category; }
    public String getDescription() { return description; }
    public String getLocation()    { return location; }
    public String getReportDate()  { return reportDate; }
    public String getReportType()  { return reportType; }
}
