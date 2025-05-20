package com.portmonitor.app.model;

import java.util.Objects;

public class Port {
    private int portNumber;
    private String protocol; // TCP, UDP
    private String state;    // LISTENING, ESTABLISHED, etc.
    private String processName;
    private int pid;

    public Port() {
    }

    public Port(int portNumber, String protocol, String state, String processName, int pid) {
        this.portNumber = portNumber;
        this.protocol = protocol;
        this.state = state;
        this.processName = processName;
        this.pid = pid;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Port port = (Port) o;
        return portNumber == port.portNumber && 
               pid == port.pid && 
               Objects.equals(protocol, port.protocol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(portNumber, protocol, pid);
    }

    @Override
    public String toString() {
        return "Port{" +
                "portNumber=" + portNumber +
                ", protocol='" + protocol + '\'' +
                ", state='" + state + '\'' +
                ", processName='" + processName + '\'' +
                ", pid=" + pid +
                '}';
    }
}