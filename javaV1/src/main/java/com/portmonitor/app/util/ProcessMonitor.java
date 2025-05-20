package com.portmonitor.app.util;

import com.portmonitor.app.model.ProcessInfo;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.ArrayList;
import java.util.List;

public class ProcessMonitor {

    private static final SystemInfo systemInfo = new SystemInfo();
    private static final HardwareAbstractionLayer hardware = systemInfo.getHardware();
    private static final OperatingSystem os = systemInfo.getOperatingSystem();
    private static final CentralProcessor processor = hardware.getProcessor();
    private static final GlobalMemory memory = hardware.getMemory();

    // 获取所有运行中的进程
    public static List<ProcessInfo> getAllProcesses() {
        List<ProcessInfo> processes = new ArrayList<>();
        List<OSProcess> osProcesses = os.getProcesses();
        
        // 获取CPU使用率需要的基准值
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        
        for (OSProcess osProcess : osProcesses) {
            try {
                int pid = osProcess.getProcessID();
                String name = osProcess.getName();
                double cpuUsage = osProcess.getProcessCpuLoadBetweenTicks(null) * 100d;
                long memoryUsage = osProcess.getResidentSetSize();
                String user = osProcess.getUser();
                String path = osProcess.getPath();
                
                ProcessInfo processInfo = new ProcessInfo(pid, name, cpuUsage, memoryUsage, user, path);
                processes.add(processInfo);
            } catch (Exception e) {
                // Skip this process if there's an error
                continue;
            }
        }
        
        return processes;
    }
}