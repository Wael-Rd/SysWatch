package com.portmonitor.service;

import com.portmonitor.model.MonitoredEntity;
import com.portmonitor.app.model.Port; 
import com.portmonitor.app.model.ProcessInfo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Service for exporting data to CSV format.
 */
public class ExportService {

    /**
     * Exports port data to CSV file.
     * @param ports List of ports to export
     * @param filePath Path to save the CSV file
     * @throws IOException if an I/O error occurs
     */
    // Change this method name from exportPortsToCsv to exportPortsToCSV
    public void exportPortsToCSV(List<Port> ports, String filePath) throws IOException {
        String[] headers = {"Port Number", "Protocol", "State", "Process Name", "PID"};
        
        try (FileWriter fileWriter = new FileWriter(filePath);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withHeader(headers))) {
            
            for (Port port : ports) {
                csvPrinter.printRecord(
                        port.getPortNumber(),
                        port.getProtocol(),
                        port.getState(),
                        port.getProcessName(),
                        port.getPid()
                );
            }
            
            csvPrinter.flush();
        }
    }

    /**
     * Exports process data to CSV file.
     * @param processes List of processes to export (ensure this is com.portmonitor.app.model.ProcessInfo)
     * @param filePath Path to save the CSV file
     * @throws IOException if an I/O error occurs
     */
    public void exportProcessesToCSV(List<com.portmonitor.app.model.ProcessInfo> processes, String filePath) throws IOException {
        String[] headers = {"PID", "Name", "CPU Usage (%)", "Memory Usage", "User", "Path"};
        
        try (FileWriter fileWriter = new FileWriter(filePath);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withHeader(headers))) {
            
            for (com.portmonitor.app.model.ProcessInfo process : processes) {
                csvPrinter.printRecord(
                        process.getPid(),
                        process.getName(),
                        String.format("%.2f", process.getCpuUsage()),
                        process.getFormattedMemoryUsage(), 
                        process.getUser(),
                        process.getPath()
                );
            }
            
            csvPrinter.flush();
        }
    }

    /**
     * Exports monitored entities to CSV file.
     * @param entities List of monitored entities to export
     * @param filePath Path to save the CSV file
     * @throws IOException if an I/O error occurs
     */
    // Change this method name from exportMonitoredEntitiesToCsv to exportMonitoredEntitiesToCSV
    public void exportMonitoredEntitiesToCSV(List<MonitoredEntity> entities, String filePath) throws IOException {
        String[] headers = {"ID", "Type", "Details", "Timestamp", "Notes"};
        
        try (FileWriter fileWriter = new FileWriter(filePath);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.withHeader(headers))) {
            
            for (MonitoredEntity entity : entities) {
                csvPrinter.printRecord(
                        entity.getId(),
                        entity.getType(),
                        entity.getDetails(),
                        entity.getTimestamp(),
                        entity.getNotes()
                );
            }
            
            csvPrinter.flush();
        }
    }
}