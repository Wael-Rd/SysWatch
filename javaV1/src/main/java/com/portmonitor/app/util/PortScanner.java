package com.portmonitor.app.util;

import com.portmonitor.app.model.Port;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PortScanner {

    // 扫描所有开放端口
    public static List<Port> scanOpenPorts() {
        List<Port> ports = new ArrayList<>();
        
        // 在Windows上使用netstat命令
        try {
            Process process = Runtime.getRuntime().exec("netstat -ano");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            // 跳过标题行
            for (int i = 0; i < 4 && reader.readLine() != null; i++) {
                // 跳过前4行
            }
            
            // 解析netstat输出
            Pattern pattern = Pattern.compile("(TCP|UDP)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([0-9]+)");
            
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String protocol = matcher.group(1);
                    String localAddress = matcher.group(2);
                    String state = matcher.group(4);
                    int pid = Integer.parseInt(matcher.group(5));
                    
                    // 提取端口号
                    int portNumber = extractPortNumber(localAddress);
                    String processName = getProcessNameByPid(pid);
                    
                    if (portNumber > 0) {
                        Port port = new Port(portNumber, protocol, state, processName, pid);
                        ports.add(port);
                    }
                }
            }
            
            process.waitFor();
            reader.close();
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        
        return ports;
    }
    
    // 从地址字符串中提取端口号
    private static int extractPortNumber(String address) {
        int colonIndex = address.lastIndexOf(':');
        if (colonIndex != -1) {
            try {
                return Integer.parseInt(address.substring(colonIndex + 1));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
    
    // 根据PID获取进程名称
    private static String getProcessNameByPid(int pid) {
        try {
            Process process = Runtime.getRuntime().exec("tasklist /FI \"PID eq " + pid + "\" /FO CSV /NH");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line = reader.readLine();
            reader.close();
            
            if (line != null && !line.isEmpty()) {
                // CSV格式，第一个字段是进程名
                String[] parts = line.split("\",\"");
                if (parts.length > 0) {
                    return parts[0].replace("\"", "");
                }
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return "Unknown";
    }
}