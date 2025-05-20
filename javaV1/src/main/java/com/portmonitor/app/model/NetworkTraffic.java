package com.portmonitor.app.model;

import java.time.LocalDateTime;

public class NetworkTraffic {
    private Long id;
    private long bytesSent;
    private long bytesReceived;
    private String protocol;
    private Integer processId;
    private Integer port;
    private LocalDateTime timestamp;
    
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
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "NetworkTraffic{" +
                "id=" + id +
                ", bytesSent=" + bytesSent +
                ", bytesReceived=" + bytesReceived +
                ", protocol='" + protocol + '\'' +
                ", processId=" + processId +
                ", port=" + port +
                ", timestamp=" + timestamp +
                '}';
    }
}