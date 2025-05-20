package com.portmonitor.model;

import java.util.Objects;

/**
 * Model class representing a system process.
 */
public class ProcessInfo {
    private int pid;
    private String name;
    private double cpuUsage; // in percentage
    private long memoryUsage; // in bytes
    private String user;
    private String path;

    public ProcessInfo() {
    }

    public ProcessInfo(int pid, String name, double cpuUsage, long memoryUsage, String user, String path) {
        this.pid = pid;
        this.name = name;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.user = user;
        this.path = path;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public long getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    // Add the missing formatting methods
    public String getFormattedCpuUsage() {
        return String.format("%.2f%%", cpuUsage);
    }

    public String getFormattedMemoryUsage() {
        if (memoryUsage < 1024) {
            return memoryUsage + " B";
        } else if (memoryUsage < 1024 * 1024) {
            return String.format("%.2f KB", memoryUsage / 1024.0);
        } else if (memoryUsage < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", memoryUsage / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", memoryUsage / (1024.0 * 1024.0 * 1024.0));
        }
    }
}