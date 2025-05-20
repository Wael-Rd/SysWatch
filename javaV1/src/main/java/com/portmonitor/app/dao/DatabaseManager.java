package com.portmonitor.app.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:monitor.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            // 确保 SQLite JDBC 驱动程序已加载
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 创建监控实体表
            String createTableSQL = ""
                    + "CREATE TABLE IF NOT EXISTS monitored_entities (" 
                    + "    id INTEGER PRIMARY KEY AUTOINCREMENT," 
                    + "    type TEXT NOT NULL," 
                    + "    name TEXT NOT NULL," 
                    + "    details TEXT NOT NULL," 
                    + "    timestamp TEXT NOT NULL" 
                    + ");";
            
            stmt.execute(createTableSQL);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}