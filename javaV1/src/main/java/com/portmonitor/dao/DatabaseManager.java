package com.portmonitor.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages database connections and initialization.
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:portmonitor.db";
    private static DatabaseManager instance;

    private DatabaseManager() {
        initDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // Create monitored_entities table
            String sql = "CREATE TABLE IF NOT EXISTS monitored_entities (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "type TEXT NOT NULL, " +  // 'PORT' or 'PROCESS'
                    "details TEXT NOT NULL, " +  // JSON string
                    "timestamp TEXT NOT NULL, " +
                    "notes TEXT)";
            stmt.execute(sql);
            // Initialize network_traffic table
            NetworkTrafficDAO networkTrafficDAO = new NetworkTrafficDAO(this);
            networkTrafficDAO.createTable();
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}