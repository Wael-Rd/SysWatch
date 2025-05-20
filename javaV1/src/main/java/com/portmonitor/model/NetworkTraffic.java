package com.portmonitor.model;

import java.time.LocalDateTime;

public class NetworkTraffic {
    private long id;
    private LocalDateTime timestamp;
    private long bytesSent;
    private long bytesReceived;
    private String protocol; // TCP, UDP
    private Integer processId; // Can be null if not associated with a specific process
    private Integer port; // Can be null if not associated with a specific port
    
    public NetworkTraffic() {
        this.timestamp = LocalDateTime.now();
    }
    
    public NetworkTraffic(long bytesSent, long bytesReceived, String protocol) {
        this();
        this.bytesSent = bytesSent;
        this.bytesReceived = bytesReceived;
        this.protocol = protocol;
    }
    
    public NetworkTraffic(long bytesSent, long bytesReceived, String protocol, Integer processId, Integer port) {
        this(bytesSent, bytesReceived, protocol);
        this.processId = processId;
        this.port = port;
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public long getBytesSent() {
        return bytesSent;
    }
    
    public void setBytesSent(long bytesSent) {
        this.bytesSent = bytesSent;
    }
    
    public long getBytesReceived() {
        return bytesReceived;
    }
    
    public void setBytesReceived(long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    public Integer getProcessId() {
        return processId;
    }
    
    public void setProcessId(Integer processId) {
        this.processId = processId;
    }
    
    public Integer getPort() {
        return port;
    }
    
    public void setPort(Integer port) {
        this.port = port;
    }
    
    @Override
    public String toString() {
        return "NetworkTraffic{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", bytesSent=" + bytesSent +
                ", bytesReceived=" + bytesReceived +
                ", protocol='" + protocol + '\'' +
                ", processId=" + processId +
                ", port=" + port +
                '}';
    }
}