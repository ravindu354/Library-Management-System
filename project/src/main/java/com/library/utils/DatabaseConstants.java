package com.library.utils;

/**
 * Database configuration constants
 * Centralized database connection parameters
 */
public class DatabaseConstants {
    
    // Database connection parameters
    public static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String URL = "jdbc:mysql://localhost:3306/library_management_system";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "2001";
    
    // Table names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_BOOKS = "books";
    public static final String TABLE_ISSUES = "book_issues";
    
    // User roles
    public static final String ROLE_STUDENT = "Student";
    public static final String ROLE_FACULTY = "Faculty";
    public static final String ROLE_LIBRARIAN = "Librarian";
    
    // Fine calculation
    public static final double FINE_PER_DAY = 1.0; // $1 per day overdue
    public static final int LOAN_PERIOD_DAYS = 14; // 2 weeks loan period
    public static final int WARNING_DAYS = 2; // Warning when due in 2 days
    
    private DatabaseConstants() {
        // Utility class - prevent instantiation
    }
}