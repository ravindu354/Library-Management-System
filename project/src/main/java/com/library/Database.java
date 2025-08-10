package com.library;

import com.library.utils.DatabaseConstants;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Database connection and management class
 * Handles all database operations using JDBC
 */
public class Database {
    
    private static Connection connection;
    
    /**
     * Initialize database connection
     */
    public static void initializeConnection() {
        try {
            Class.forName(DatabaseConstants.DRIVER);
            connection = DriverManager.getConnection(
                DatabaseConstants.URL, 
                DatabaseConstants.USERNAME, 
                DatabaseConstants.PASSWORD
            );
            System.out.println("Database connection established successfully!");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get current database connection
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initializeConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
    
    /**
     * Execute SELECT query and return ResultSet
     */
    public static ResultSet executeQuery(String query, Object... params) {
        try {
            PreparedStatement stmt = getConnection().prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Execute INSERT, UPDATE, DELETE queries
     */
    public static int executeUpdate(String query, Object... params) {
        try {
            PreparedStatement stmt = getConnection().prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * Execute INSERT and return generated key
     */
    public static long executeInsertWithGeneratedKey(String query, Object... params) {
        try {
            PreparedStatement stmt = getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    /**
     * Close database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}