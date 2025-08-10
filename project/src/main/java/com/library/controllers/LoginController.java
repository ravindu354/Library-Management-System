package com.library.controllers;

import com.library.Database;
import com.library.Main;
import com.library.models.User;
import com.library.models.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Controller for the login screen
 * Handles user authentication and role-based access
 */
public class LoginController implements Initializable {
    
    @FXML private VBox loginContainer;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Label titleLabel;
    
    private static User currentUser;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize UI components
        errorLabel.setVisible(false);
        
        // Add enter key support for login
        usernameField.setOnAction(e -> handleLogin());
        passwordField.setOnAction(e -> handleLogin());
        
        // Focus on username field
        usernameField.requestFocus();
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }
        
        // Authenticate user
        User user = authenticateUser(username, password);
        if (user != null) {
            currentUser = user;
            try {
                Main.showDashboard(user.getRole().getDisplayName());
            } catch (Exception e) {
                showError("Error loading dashboard: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showError("Invalid username or password.");
            passwordField.clear();
        }
    }
    
    /**
     * Authenticate user against database
     */
    private User authenticateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ? AND is_active = 1";
        
        try {
            ResultSet rs = Database.executeQuery(query, username, password);
            if (rs != null && rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setRole(UserRole.fromString(rs.getString("role")));
                user.setIsActive(rs.getBoolean("is_active"));
                
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database error occurred during authentication.");
        }
        
        return null;
    }
    
    /**
     * Display error message
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    /**
     * Get currently logged in user
     */
    public static User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Logout current user
     */
    public static void logout() {
        currentUser = null;
        try {
            Main.showLoginScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}