package com.portmonitor.service;

import com.portmonitor.app.util.PortScanner;
// Change this import
// import com.portmonitor.model.Port;
// To this:
import com.portmonitor.app.model.Port;

import java.util.List;

public class PortService {
    public List<Port> getAllPorts() {
        return PortScanner.scanOpenPorts();
    }
    
    /**
     * Gets a list of currently open ports on the system.
     * @return List of Port objects representing open ports
     */
    public List<Port> getOpenPorts() {
        // This method uses the same implementation as getAllPorts
        // since we're only interested in open ports
        return PortScanner.scanOpenPorts();
    }
}