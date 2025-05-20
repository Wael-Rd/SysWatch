package com.portmonitor.app.model;

import java.util.Objects;

public class ProcessInfo {
    private int pid;
    private String name;
    private double cpuUsage; // 百分比
    private long memoryUsage; // 字节
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

    // 格式化内存使用量为可读格式（MB）
    public String getFormattedMemory() {
        return String.format("%.2f MB", memoryUsage / (1024.0 * 1024.0));
    }

    // 格式化CPU使用率
    public String getFormattedCpuUsage() {
        return String.format("%.2f%%", cpuUsage);
    }

    // Add this method
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessInfo that = (ProcessInfo) o;
        return pid == that.pid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid);
    }

    @Override
    public String toString() {
        return "ProcessInfo{" +
                "pid=" + pid +
                ", name='" + name + '\'' +
                ", cpuUsage=" + cpuUsage +
                ", memoryUsage=" + memoryUsage +
                ", user='" + user + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}