package com.portmonitor.app.dao;

import com.portmonitor.app.model.MonitoredEntity;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MonitoredEntityDAO {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 创建新的监控实体
    public boolean create(MonitoredEntity entity) {
        String sql = "INSERT INTO monitored_entities (type, name, details, timestamp) VALUES (?, ?, ?, ?)"; 
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, entity.getType().toString());
            pstmt.setString(2, entity.getName());
            pstmt.setString(3, entity.getDetails());
            pstmt.setString(4, entity.getTimestamp().format(formatter));
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        entity.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }

    // 根据ID获取监控实体
    public MonitoredEntity getById(int id) {
        String sql = "SELECT * FROM monitored_entities WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractEntityFromResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    // 获取所有监控实体
    public List<MonitoredEntity> getAll() {
        List<MonitoredEntity> entities = new ArrayList<>();
        String sql = "SELECT * FROM monitored_entities ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                entities.add(extractEntityFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return entities;
    }

    // 根据类型获取监控实体
    public List<MonitoredEntity> getByType(MonitoredEntity.EntityType type) {
        List<MonitoredEntity> entities = new ArrayList<>();
        String sql = "SELECT * FROM monitored_entities WHERE type = ? ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, type.toString());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    entities.add(extractEntityFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return entities;
    }

    // 更新监控实体
    public boolean update(MonitoredEntity entity) {
        String sql = "UPDATE monitored_entities SET type = ?, name = ?, details = ?, timestamp = ? WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, entity.getType().toString());
            pstmt.setString(2, entity.getName());
            pstmt.setString(3, entity.getDetails());
            pstmt.setString(4, entity.getTimestamp().format(formatter));
            pstmt.setInt(5, entity.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }

    // 删除监控实体
    public boolean delete(int id) {
        String sql = "DELETE FROM monitored_entities WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }

    // 从ResultSet中提取实体
    private MonitoredEntity extractEntityFromResultSet(ResultSet rs) throws SQLException {
        MonitoredEntity entity = new MonitoredEntity();
        entity.setId(rs.getInt("id"));
        entity.setType(MonitoredEntity.EntityType.valueOf(rs.getString("type")));
        entity.setName(rs.getString("name"));
        entity.setDetails(rs.getString("details"));
        entity.setTimestamp(LocalDateTime.parse(rs.getString("timestamp"), formatter));
        return entity;
    }
}