package com.portmonitor.dao;

// Change this import
// import com.portmonitor.app.DatabaseManager;
// To this:
import com.portmonitor.dao.DatabaseManager;

import com.portmonitor.model.MonitoredEntity;
import com.portmonitor.app.model.Port; 
// Change this import from com.portmonitor.model.ProcessInfo to com.portmonitor.app.model.ProcessInfo
import com.portmonitor.app.model.ProcessInfo;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MonitoredEntityDao {
    // Change this line
    // private final DatabaseManager dbManager = new DatabaseManager();
    // To this:
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    public List<MonitoredEntity> getAllEntities() throws SQLException {
        List<MonitoredEntity> entities = new ArrayList<>();
        String query = "SELECT * FROM monitored_entities ORDER BY timestamp DESC";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                MonitoredEntity entity = new MonitoredEntity();
                entity.setId(rs.getInt("id"));
                entity.setType(MonitoredEntity.EntityType.valueOf(rs.getString("type")));
                entity.setDetails(rs.getString("details"));
                entity.setName(rs.getString("name"));
                // Convert timestamp from database to LocalDateTime
                entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                entities.add(entity);
            }
        }
        
        return entities;
    }

    public void addPort(Port port) throws SQLException {
        String details = String.format("Port: %d, Protocol: %s, State: %s, PID: %d",
                port.getPortNumber(), port.getProtocol(), port.getState(), port.getPid());
        
        String query = "INSERT INTO monitored_entities (type, details, name, timestamp) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, MonitoredEntity.EntityType.PORT.toString());
            pstmt.setString(2, details);
            pstmt.setString(3, port.getProcessName());
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            
            pstmt.executeUpdate();
        }
    }

    // Ensure this method accepts com.portmonitor.app.model.ProcessInfo
    public void addProcess(com.portmonitor.app.model.ProcessInfo process) throws SQLException {
        String details = String.format("PID: %d, CPU: %s, Memory: %s, User: %s",
                process.getPid(), process.getFormattedCpuUsage(), 
                process.getFormattedMemoryUsage(), process.getUser());
        
        String query = "INSERT INTO monitored_entities (type, details, name, timestamp) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection(); // Assuming DatabaseManager is a singleton
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, MonitoredEntity.EntityType.PROCESS.toString());
            pstmt.setString(2, details);
            pstmt.setString(3, process.getName());
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            
            pstmt.executeUpdate();
        }
    }

    public void deleteEntity(int id) throws SQLException {
        String query = "DELETE FROM monitored_entities WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}