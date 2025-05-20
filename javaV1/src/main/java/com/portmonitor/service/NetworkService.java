package com.portmonitor.service;

import com.portmonitor.dao.DatabaseManager;
import com.portmonitor.dao.NetworkTrafficDAO;
import com.portmonitor.model.NetworkTraffic;
import com.portmonitor.model.Port;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkService {
    // Update this line to pass DatabaseManager.getInstance()
    private final NetworkTrafficDAO networkTrafficDAO = new NetworkTrafficDAO(DatabaseManager.getInstance());
    private final SystemInfo systemInfo = new SystemInfo();
    private final HardwareAbstractionLayer hardware = systemInfo.getHardware();
    
    // Cache for previous network stats to calculate deltas
    private final Map<String, NetworkStats> previousStats = new HashMap<>();
    
    // Inner class to hold network stats
    private static class NetworkStats {
        long bytesSent;
        long bytesReceived;
        long timestamp;
        
        NetworkStats(long bytesSent, long bytesReceived) {
            this.bytesSent = bytesSent;
            this.bytesReceived = bytesReceived;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    public List<NetworkTraffic> collectNetworkTraffic() {
        List<NetworkTraffic> trafficList = new ArrayList<>();
        List<NetworkIF> networkInterfaces = hardware.getNetworkIFs();
        
        for (NetworkIF networkInterface : networkInterfaces) {
            // Skip loopback interfaces
            if (networkInterface.getName().contains("lo") || 
                networkInterface.getIPv4addr().length == 0) {
                continue;
            }
            
            // Get current stats
            networkInterface.updateAttributes();
            long bytesSent = networkInterface.getBytesSent();
            long bytesReceived = networkInterface.getBytesRecv();
            String interfaceName = networkInterface.getName();
            
            // Calculate delta if we have previous stats
            if (previousStats.containsKey(interfaceName)) {
                NetworkStats prev = previousStats.get(interfaceName);
                long sentDelta = bytesSent - prev.bytesSent;
                long receivedDelta = bytesReceived - prev.bytesReceived;
                
                // Only record if there's actual traffic
                if (sentDelta > 0 || receivedDelta > 0) {
                    // Create a generic entry for the interface
                    // Use the appropriate constructor
                    NetworkTraffic traffic = new NetworkTraffic(sentDelta, receivedDelta, interfaceName);
                    trafficList.add(traffic);
                    
                    // Save to database
                    networkTrafficDAO.save(traffic);
                }
            }
            
            // Update previous stats
            previousStats.put(interfaceName, new NetworkStats(bytesSent, bytesReceived));
        }
        
        return trafficList;
    }
    
    // Associate network traffic with ports and processes
    public void associateTrafficWithPortsAndProcesses(List<NetworkTraffic> trafficList, List<Port> ports) {
        // This is a simplified implementation
        // In a real implementation, you would need to use tools like netstat or JNA to get
        // per-process and per-connection network statistics
        
        // For demonstration purposes, we'll just randomly associate traffic with ports
        for (NetworkTraffic traffic : trafficList) {
            if (!ports.isEmpty()) {
                // Just associate with the first port for demonstration
                Port port = ports.get(0);
                traffic.setPort(port.getPortNumber());
                traffic.setProcessId(port.getPid());
                traffic.setProtocol(port.getProtocol());
                
                // Update in database
                networkTrafficDAO.save(traffic);
            }
        }
    }
    
    public List<NetworkTraffic> getRecentTraffic(int limit) {
        return networkTrafficDAO.getRecentTraffic(limit);
    }
    
    public List<NetworkTraffic> getTrafficByProtocol(String protocol, int limit) {
        return networkTrafficDAO.getTrafficByProtocol(protocol, limit);
    }
    
    public List<NetworkTraffic> getTrafficByProcess(int processId, int limit) {
        return networkTrafficDAO.getTrafficByProcess(processId, limit);
    }
    
    public List<NetworkTraffic> getTrafficByPort(int port, int limit) {
        return networkTrafficDAO.getTrafficByPort(port, limit);
    }
}