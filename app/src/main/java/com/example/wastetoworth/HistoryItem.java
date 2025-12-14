package com.example.wastetoworth;

import java.util.Date;

public class HistoryItem {
    private String id;
    private String title;
    private String description;
    private Date date;
    private String status; // e.g., "Completed", "In Progress", "Failed"
    private String type;   // e.g., "Donation", "Request", "Volunteer"

    // Empty constructor required for Firestore
    public HistoryItem() {
    }

    public HistoryItem(String id, String title, String description, Date date, String status, String type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.status = status;
        this.type = type;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }
    
    public Date getTimestamp() {
        return date; // Return the same as getDate() for backward compatibility
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
