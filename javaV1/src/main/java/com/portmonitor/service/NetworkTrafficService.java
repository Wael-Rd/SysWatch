package com.portmonitor.service;

import com.portmonitor.dao.DatabaseManager;
import com.portmonitor.dao.NetworkTrafficDAO;
import com.portmonitor.model.NetworkTraffic;
// Change this import
// import com.portmonitor.model.Port;
// To this:
import com.portmonitor.app.model.Port;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkTrafficService {
    private final NetworkTrafficDAO networkTrafficDAO;
    private final PortService portService;
    private final SystemInfo systemInfo;
    private final HardwareAbstractionLayer hardware;
    
    // Cache for previous network stats to calculate deltas
    private Map<String, NetworkStats> previousStats;
    
    public NetworkTrafficService() {
        // Use DatabaseManager.getInstance() to get the singleton instance
        this.networkTrafficDAO = new NetworkTrafficDAO(DatabaseManager.getInstance());
        this.portService = new PortService();
        this.systemInfo = new SystemInfo();
        this.hardware = systemInfo.getHardware();
        this.previousStats = new HashMap<>();
    }
    
    public List<NetworkTraffic> collectNetworkTraffic() {
        List<NetworkTraffic> trafficData = new ArrayList<>();
        List<Port> activePorts = portService.getOpenPorts();
        
        // Get all network interfaces
        List<NetworkIF> networkInterfaces = hardware.getNetworkIFs();
        // Convert List to array if needed for compatibility with older code
        NetworkIF[] networkInterfacesArray = networkInterfaces.toArray(new NetworkIF[0]);
        
        for (NetworkIF networkInterface : networkInterfaces) {
            // Update stats to get current values
            networkInterface.updateAttributes();
            
            String interfaceName = networkInterface.getName();
            long bytesSent = networkInterface.getBytesSent();
            long bytesReceived = networkInterface.getBytesRecv();
            
            // Calculate delta if we have previous stats
            long deltaBytesSent = 0;
            long deltaBytesReceived = 0;
            
            if (previousStats.containsKey(interfaceName)) {
                NetworkStats prevStats = previousStats.get(interfaceName);
                deltaBytesSent = bytesSent - prevStats.bytesSent;
                deltaBytesReceived = bytesReceived - prevStats.bytesReceived;
            }
            
            // Store current stats for next calculation
            previousStats.put(interfaceName, new NetworkStats(bytesSent, bytesReceived));
            
            // Only record if there's actual traffic
            if (deltaBytesSent > 0 || deltaBytesReceived > 0) {
                // Create a generic entry for the interface
                NetworkTraffic traffic = new NetworkTraffic(deltaBytesSent, deltaBytesReceived, "ALL");
                trafficData.add(traffic);
                
                // Save to database
                networkTrafficDAO.save(traffic);
                
                // Try to associate with TCP/UDP ports if possible
                for (Port port : activePorts) {
                    // This is a simplification - in a real app, you'd need to determine
                    // which traffic belongs to which port/process more accurately
                    NetworkTraffic portTraffic = new NetworkTraffic(
                            deltaBytesSent / activePorts.size(),
                            deltaBytesReceived / activePorts.size(),
                            port.getProtocol(),
                            port.getPid(),
                            port.getPortNumber());
                    
                    trafficData.add(portTraffic);
                    networkTrafficDAO.save(portTraffic);
                }
            }
        }
        
        return trafficData;
    }
    
    public List<NetworkTraffic> getTrafficByProtocol(String protocol) {
        return networkTrafficDAO.getByProtocol(protocol);
    }
    
    public List<NetworkTraffic> getTrafficByProcessId(int processId) {
        return networkTrafficDAO.getByProcessId(processId);
    }
    
    public List<NetworkTraffic> getTrafficByPort(int port) {
        return networkTrafficDAO.getByPort(port);
    }
    
    /**
     * Gets traffic data filtered by protocol with a limit.
     * @param protocol The protocol to filter by
     * @return List of NetworkTraffic objects
     */
    public List<NetworkTraffic> getTrafficByProtocol(String protocol, int limit) {
        return networkTrafficDAO.getTrafficByProtocol(protocol, limit);
    }
    
    /**
     * Gets traffic data filtered by process ID with a limit.
     * @param processId The process ID to filter by
     * @param limit The maximum number of records to return
     * @return List of NetworkTraffic objects
     */
    public List<NetworkTraffic> getTrafficByProcessId(int processId, int limit) {
        return networkTrafficDAO.getTrafficByProcess(processId, limit);
    }
    
    /**
     * Gets traffic data filtered by port with a limit.
     * @param port The port to filter by
     * @param limit The maximum number of records to return
     * @return List of NetworkTraffic objects
     */
    public List<NetworkTraffic> getTrafficByPort(int port, int limit) {
        return networkTrafficDAO.getTrafficByPort(port, limit);
    }
    
    /**
     * Gets recent network traffic data limited by time.
     * @param minutes Number of minutes to look back
     * @return List of NetworkTraffic objects
     */
    public List<NetworkTraffic> getRecentTraffic(int minutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutes);
        return networkTrafficDAO.getByTimeRange(cutoff, LocalDateTime.now());
    }
    
    public boolean cleanupOldData(int retentionDays) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        return networkTrafficDAO.deleteOlderThan(cutoff);
    }
    
    // Helper class to store previous network stats
    private static class NetworkStats {
        private final long bytesSent;
        private final long bytesReceived;
        
        public NetworkStats(long bytesSent, long bytesReceived) {
            this.bytesSent = bytesSent;
            this.bytesReceived = bytesReceived;
        }
    }
}