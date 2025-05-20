package com.portmonitor.model;

import java.time.LocalDateTime;

public class MonitoredEntity {
    private int id;
    private EntityType type;
    private String details;
    private LocalDateTime timestamp;
    private String name;
    private String notes; // Add this field

    // Add a new EntityType for network traffic
    public enum EntityType {
        PORT, PROCESS, NETWORK_TRAFFIC
    }

    public MonitoredEntity() {
        this.timestamp = LocalDateTime.now();
    }

    public MonitoredEntity(EntityType type, String details, String name) {
        this.type = type;
        this.details = details;
        this.name = name;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Add these methods
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}