package com.portmonitor.dao;

import com.portmonitor.model.NetworkTraffic;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NetworkTrafficDAO {
    private final DatabaseManager dbManager;
    
    // Remove the default constructor to prevent circular dependency
    // public NetworkTrafficDAO() {
    //     this(DatabaseManager.getInstance());
    // }
    public NetworkTrafficDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        initTable();
    }
    
    private void initTable() {
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {
            
            String sql = "CREATE TABLE IF NOT EXISTS network_traffic (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "timestamp TEXT NOT NULL, " +
                    "bytes_sent INTEGER NOT NULL, " +
                    "bytes_received INTEGER NOT NULL, " +
                    "protocol TEXT NOT NULL, " +
                    "process_id INTEGER, " +
                    "port INTEGER)";
            
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Creates the network_traffic table if it doesn't exist.
     * This method is called from DatabaseManager during initialization.
     */
    public void createTable() {
        // Reuse the initTable method since it already has the table creation logic
        initTable();
    }
    
    public boolean save(NetworkTraffic traffic) {
        String sql = "INSERT INTO network_traffic (timestamp, bytes_sent, bytes_received, protocol, process_id, port) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, traffic.getTimestamp().toString());
            pstmt.setLong(2, traffic.getBytesSent());
            pstmt.setLong(3, traffic.getBytesReceived());
            pstmt.setString(4, traffic.getProtocol());
            
            if (traffic.getProcessId() != null) {
                pstmt.setInt(5, traffic.getProcessId());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            
            if (traffic.getPort() != null) {
                pstmt.setInt(6, traffic.getPort());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<NetworkTraffic> getAll() {
        List<NetworkTraffic> trafficList = new ArrayList<>();
        String sql = "SELECT * FROM network_traffic ORDER BY timestamp DESC";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                trafficList.add(extractFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return trafficList;
    }
    
    public List<NetworkTraffic> getByTimeRange(LocalDateTime start, LocalDateTime end) {
        List<NetworkTraffic> trafficList = new ArrayList<>();
        String sql = "SELECT * FROM network_traffic WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, start.toString());
            pstmt.setString(2, end.toString());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    trafficList.add(extractFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return trafficList;
    }
    
    /**
     * Gets the most recent network traffic data.
     * @param limit The maximum number of records to return
     * @return List of NetworkTraffic objects
     */
    public List<NetworkTraffic> getRecentTraffic(int limit) {
        List<NetworkTraffic> trafficList = new ArrayList<>();
        String sql = "SELECT * FROM network_traffic ORDER BY timestamp DESC LIMIT ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    trafficList.add(extractFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return trafficList;
    }
    
    /**
     * Gets network traffic data filtered by protocol.
     * @param protocol The protocol to filter by (e.g., "TCP", "UDP")
     * @param limit The maximum number of records to return
     * @return List of NetworkTraffic objects
     */
    public List<NetworkTraffic> getTrafficByProtocol(String protocol, int limit) {
        List<NetworkTraffic> trafficList = new ArrayList<>();
        String sql = "SELECT * FROM network_traffic WHERE protocol = ? ORDER BY timestamp DESC LIMIT ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, protocol);
            pstmt.setInt(2, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    trafficList.add(extractFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return trafficList;
    }
    
    /**
     * Gets network traffic data filtered by process ID.
     * @param processId The process ID to filter by
     * @param limit The maximum number of records to return
     * @return List of NetworkTraffic objects
     */
    public List<NetworkTraffic> getTrafficByProcess(int processId, int limit) {
        List<NetworkTraffic> trafficList = new ArrayList<>();
        String sql = "SELECT * FROM network_traffic WHERE process_id = ? ORDER BY timestamp DESC LIMIT ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, processId);
            pstmt.setInt(2, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    trafficList.add(extractFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return trafficList;
    }
    
    /**
     * Gets network traffic data filtered by port number.
     * @param port The port number to filter by
     * @param limit The maximum number of records to return
     * @return List of NetworkTraffic objects
     */
    public List<NetworkTraffic> getTrafficByPort(int port, int limit) {
        List<NetworkTraffic> trafficList = new ArrayList<>();
        String sql = "SELECT * FROM network_traffic WHERE port = ? ORDER BY timestamp DESC LIMIT ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, port);
            pstmt.setInt(2, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    trafficList.add(extractFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return trafficList;
    }
    
    public List<NetworkTraffic> getByProtocol(String protocol) {
        List<NetworkTraffic> trafficList = new ArrayList<>();
        String sql = "SELECT * FROM network_traffic WHERE protocol = ? ORDER BY timestamp DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, protocol);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    trafficList.add(extractFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return trafficList;
    }
    
    public List<NetworkTraffic> getByProcessId(int processId) {
        List<NetworkTraffic> trafficList = new ArrayList<>();
        String sql = "SELECT * FROM network_traffic WHERE process_id = ? ORDER BY timestamp DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, processId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    trafficList.add(extractFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return trafficList;
    }
    
    public List<NetworkTraffic> getByPort(int port) {
        List<NetworkTraffic> trafficList = new ArrayList<>();
        String sql = "SELECT * FROM network_traffic WHERE port = ? ORDER BY timestamp DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, port);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    trafficList.add(extractFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return trafficList;
    }
    
    public boolean deleteOlderThan(LocalDateTime cutoff) {
        String sql = "DELETE FROM network_traffic WHERE timestamp < ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cutoff.toString());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private NetworkTraffic extractFromResultSet(ResultSet rs) throws SQLException {
        NetworkTraffic traffic = new NetworkTraffic();
        traffic.setId(rs.getLong("id"));
        traffic.setTimestamp(LocalDateTime.parse(rs.getString("timestamp")));
        traffic.setBytesSent(rs.getLong("bytes_sent"));
        traffic.setBytesReceived(rs.getLong("bytes_received"));
        traffic.setProtocol(rs.getString("protocol"));
        
        int processId = rs.getInt("process_id");
        if (!rs.wasNull()) {
            traffic.setProcessId(processId);
        }
        
        int port = rs.getInt("port");
        if (!rs.wasNull()) {
            traffic.setPort(port);
        }
        
        return traffic;
    }
}