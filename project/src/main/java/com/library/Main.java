package com.library;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application launcher for Library Management System
 * Initializes JavaFX application and loads the login screen
 */
public class Main extends Application {
    
    private static Stage primaryStage;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.primaryStage = primaryStage;
        
        // Initialize database connection
        Database.initializeConnection();
        
        // Load login screen
        showLoginScreen();
    }
    
    /**
     * Display the login screen
     */
    public static void showLoginScreen() throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/Login.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add(Main.class.getResource("/css/login.css").toExternalForm());
        
        primaryStage.setTitle("Library Management System - Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }
    
    /**
     * Switch to dashboard with role-specific styling
     */
    public static void showDashboard(String userRole) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/Dashboard.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 1024, 768);
        
        // Apply role-specific CSS
        String cssFile = switch (userRole.toLowerCase()) {
            case "student" -> "/css/student.css";
            case "faculty" -> "/css/faculty.css";
            case "librarian" -> "/css/librarian.css";
            default -> "/css/student.css";
        };
        
        scene.getStylesheets().add(Main.class.getResource(cssFile).toExternalForm());
        
        primaryStage.setTitle("Library Management System - Dashboard (" + userRole + ")");
        primaryStage.setScene(scene);
    }
    
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}