package com.portmonitor.service;

// Change this import:
import com.portmonitor.app.model.ProcessInfo;
// Instead of:
// import com.portmonitor.model.ProcessInfo;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for retrieving process information using OSHI.
 */
public class ProcessService {
    private final SystemInfo systemInfo;
    private final OperatingSystem os;
    private final HardwareAbstractionLayer hardware;
    private final CentralProcessor processor;
    private final GlobalMemory memory;

    public ProcessService() {
        this.systemInfo = new SystemInfo();
        this.os = systemInfo.getOperatingSystem();
        this.hardware = systemInfo.getHardware();
        this.processor = hardware.getProcessor();
        this.memory = hardware.getMemory();
    }

    /**
     * Gets information about all running processes.
     * @return List of ProcessInfo objects
     */
    public List<ProcessInfo> getAllProcesses() {
        List<ProcessInfo> processes = new ArrayList<>();
        List<OSProcess> osProcesses = os.getProcesses();
        
        // Get processor count for CPU usage calculation
        int processorCount = processor.getLogicalProcessorCount();
        
        for (OSProcess osProcess : osProcesses) {
            int pid = osProcess.getProcessID();
            String name = osProcess.getName();
            double cpuUsage = 100d * osProcess.getProcessCpuLoadCumulative() / processorCount;
            long memoryUsage = osProcess.getResidentSetSize();
            String user = osProcess.getUser();
            String path = osProcess.getPath();
            
            ProcessInfo processInfo = new ProcessInfo(pid, name, cpuUsage, memoryUsage, user, path);
            processes.add(processInfo);
        }
        
        return processes;
    }
    
    /**
     * Gets information about currently running processes.
     * @return List of ProcessInfo objects for running processes
     */
    public List<ProcessInfo> getRunningProcesses() {
        List<ProcessInfo> processes = new ArrayList<>();
        List<OSProcess> osProcesses = os.getProcesses();
        
        // Get processor count for CPU usage calculation
        int processorCount = processor.getLogicalProcessorCount();
        
        for (OSProcess osProcess : osProcesses) {
            // Only include running processes
            if (osProcess.getState() == OSProcess.State.RUNNING) {
                int pid = osProcess.getProcessID();
                String name = osProcess.getName();
                double cpuUsage = 100d * osProcess.getProcessCpuLoadCumulative() / processorCount;
                long memoryUsage = osProcess.getResidentSetSize();
                String user = osProcess.getUser();
                String path = osProcess.getPath();
                
                ProcessInfo processInfo = new ProcessInfo(pid, name, cpuUsage, memoryUsage, user, path);
                processes.add(processInfo);
            }
        }
        
        return processes;
    }

    /**
     * Gets information about a specific process by PID.
     * @param pid Process ID
     * @return ProcessInfo object or null if not found
     */
    public ProcessInfo getProcessById(int pid) {
        OSProcess osProcess = os.getProcess(pid);
        
        if (osProcess != null) {
            int processorCount = processor.getLogicalProcessorCount();
            double cpuUsage = 100d * osProcess.getProcessCpuLoadCumulative() / processorCount;
            long memoryUsage = osProcess.getResidentSetSize();
            String user = osProcess.getUser();
            String path = osProcess.getPath();
            
            return new ProcessInfo(pid, osProcess.getName(), cpuUsage, memoryUsage, user, path);
        }
        
        return null;
    }
}