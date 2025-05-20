package com.portmonitor.app.service;

import com.portmonitor.app.model.NetworkTraffic;
import com.portmonitor.app.model.Port;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Service for collecting and managing network traffic data.
 */
public class NetworkTrafficService {
    
    private final Random random = new Random(); // For demo data generation
    private List<NetworkTraffic> cachedTrafficData = new ArrayList<>();
    
    /**
     * Collects current network traffic data.
     * In a real application, this would use system APIs to get actual network traffic.
     * For this demo, we generate random data.
     * 
     * @return List of NetworkTraffic objects
     */
    public List<NetworkTraffic> collectNetworkTraffic() {
        List<NetworkTraffic> trafficData = new ArrayList<>();
        
        // Generate some random traffic data for demonstration
        // In a real app, you would use JNA, JNI, or ProcessBuilder to get actual network stats
        
        // Overall traffic (all interfaces)
        long deltaBytesSent = random.nextInt(100000);
        long deltaBytesReceived = random.nextInt(150000);
        
        NetworkTraffic traffic = new NetworkTraffic(deltaBytesSent, deltaBytesReceived, "ALL");
        trafficData.add(traffic);
        
        // Add to cached data
        cachedTrafficData.add(traffic);
        
        // Limit cache size
        if (cachedTrafficData.size() > 3600) { // Keep last hour at 1 sample/second
            cachedTrafficData.remove(0);
        }
        
        return trafficData;
    }
    
    /**
     * Gets traffic data filtered by protocol.
     * 
     * @param protocol Protocol to filter by
     * @return List of NetworkTraffic objects
     */
    public List<NetworkTraffic> getTrafficByProtocol(String protocol) {
        List<NetworkTraffic> result = new ArrayList<>();
        for (NetworkTraffic traffic : cachedTrafficData) {
            if (protocol.equals(traffic.getProtocol())) {
                result.add(traffic);
            }
        }
        return result;
    }
    
    /**
     * Gets traffic data filtered by process ID.
     * 
     * @param processId Process ID to filter by
     * @return List of NetworkTraffic objects
     */
    public List<NetworkTraffic> getTrafficByProcessId(int processId) {
        List<NetworkTraffic> result = new ArrayList<>();
        for (NetworkTraffic traffic : cachedTrafficData) {
            if (traffic.getProcessId() != null && traffic.getProcessId() == processId) {
                result.add(traffic);
            }
        }
        return result;
    }
    
    /**
     * Gets traffic data filtered by port.
     * 
     * @param port Port to filter by
     * @return List of NetworkTraffic objects
     */
    public List<NetworkTraffic> getTrafficByPort(int port) {
        List<NetworkTraffic> result = new ArrayList<>();
        for (NetworkTraffic traffic : cachedTrafficData) {
            if (traffic.getPort() != null && traffic.getPort() == port) {
                result.add(traffic);
            }
        }
        return result;
    }
    
    /**
     * Gets recent traffic data from the last specified number of minutes.
     * 
     * @param minutes Number of minutes to look back
     * @return List of NetworkTraffic objects
     */
    public List<NetworkTraffic> getRecentTraffic(int minutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutes);
        List<NetworkTraffic> result = new ArrayList<>();
        
        for (NetworkTraffic traffic : cachedTrafficData) {
            if (traffic.getTimestamp().isAfter(cutoff)) {
                result.add(traffic);
            }
        }
        
        return result;
    }
    
    /**
     * Gets a list of active process IDs as strings.
     * For demo, returns dummy data.
     */
    public List<String> getActiveProcesses() {
        // In real app, query system for active processes
        return cachedTrafficData.stream()
                .map(t -> t.getProcessId() != null ? String.valueOf(t.getProcessId()) : null)
                .filter(p -> p != null)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Gets a list of active ports as strings.
     * For demo, returns dummy data.
     */
    public List<String> getActivePorts() {
        // In real app, query system for active ports
        return cachedTrafficData.stream()
                .map(t -> t.getPort() != null ? String.valueOf(t.getPort()) : null)
                .filter(p -> p != null)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Exports cached traffic data to CSV file.
     * 
     * @param filePath Path to output CSV file
     * @throws IOException if file write fails
     */
    public void exportToCSV(String filePath) throws IOException {
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(filePath), CSVFormat.DEFAULT
                .withHeader("Timestamp", "BytesSent", "BytesReceived", "Protocol", "ProcessId", "Port"))) {
            for (NetworkTraffic traffic : cachedTrafficData) {
                printer.printRecord(
                        traffic.getTimestamp(),
                        traffic.getBytesSent(),
                        traffic.getBytesReceived(),
                        traffic.getProtocol(),
                        traffic.getProcessId(),
                        traffic.getPort()
                );
            }
        }
    }
}
