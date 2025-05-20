package com.portmonitor.app.model;

import java.time.LocalDateTime;

public class MonitoredEntity {
    private int id;
    private EntityType type; // PORT 或 PROCESS
    private String details; // JSON 格式的详细信息
    private LocalDateTime timestamp;
    private String name; // 实体名称（端口号或进程名）

    public enum EntityType {
        PORT, PROCESS
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

    @Override
    public String toString() {
        return "MonitoredEntity{" +
                "id=" + id +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}