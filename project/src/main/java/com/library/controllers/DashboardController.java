package com.library.controllers;

import com.library.Database;
import com.library.Main;
import com.library.models.Issue;
import com.library.models.User;
import com.library.models.UserRole;
import com.library.utils.DateUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Controller for the main dashboard
 * Provides navigation and overview of library activities
 */
public class DashboardController implements Initializable {
    
    @FXML private Label welcomeLabel;
    @FXML private Label userRoleLabel;
    @FXML private VBox mainContent;
    @FXML private HBox navigationBar;
    
    // Navigation buttons
    @FXML private Button booksButton;
    @FXML private Button usersButton;
    @FXML private Button issueReturnButton;
    @FXML private Button reportsButton;
    @FXML private Button logoutButton;
    
    // Dashboard statistics
    @FXML private Label totalBooksLabel;
    @FXML private Label availableBooksLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label activeIssuesLabel;
    @FXML private Label overdueIssuesLabel;
    
    // Notifications area
    @FXML private VBox notificationsArea;
    @FXML private ListView<String> notificationsList;
    
    private User currentUser;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = LoginController.getCurrentUser();
        
        if (currentUser != null) {
            setupUserInterface();
            loadDashboardStatistics();
            loadNotifications();
        }
    }
    
    /**
     * Setup UI based on user role
     */
    private void setupUserInterface() {
        welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        userRoleLabel.setText("Role: " + currentUser.getRole().getDisplayName());
        
        // Configure navigation based on user role
        UserRole role = currentUser.getRole();
        
        // All users can access books and issue/return
        booksButton.setVisible(true);
        issueReturnButton.setVisible(true);
        
        // Only librarians can manage users and view all reports
        usersButton.setVisible(role == UserRole.LIBRARIAN);
        reportsButton.setVisible(role == UserRole.LIBRARIAN || role == UserRole.FACULTY);
        
        // Set button actions
        booksButton.setOnAction(e -> openBooksWindow());
        usersButton.setOnAction(e -> openUsersWindow());
        issueReturnButton.setOnAction(e -> openIssueReturnWindow());
        reportsButton.setOnAction(e -> openReportsWindow());
        logoutButton.setOnAction(e -> LoginController.logout());
    }
    
    /**
     * Load dashboard statistics
     */
    private void loadDashboardStatistics() {
        try {
            // Total books
            ResultSet rs = Database.executeQuery("SELECT COUNT(*) as count FROM books WHERE is_active = 1");
            if (rs != null && rs.next()) {
                totalBooksLabel.setText(String.valueOf(rs.getInt("count")));
            }
            
            // Available books
            rs = Database.executeQuery("SELECT SUM(available_copies) as count FROM books WHERE is_active = 1");
            if (rs != null && rs.next()) {
                availableBooksLabel.setText(String.valueOf(rs.getInt("count")));
            }
            
            // Total users
            rs = Database.executeQuery("SELECT COUNT(*) as count FROM users WHERE is_active = 1");
            if (rs != null && rs.next()) {
                totalUsersLabel.setText(String.valueOf(rs.getInt("count")));
            }
            
            // Active issues
            rs = Database.executeQuery("SELECT COUNT(*) as count FROM book_issues WHERE is_returned = 0");
            if (rs != null && rs.next()) {
                activeIssuesLabel.setText(String.valueOf(rs.getInt("count")));
            }
            
            // Overdue issues
            rs = Database.executeQuery("SELECT COUNT(*) as count FROM book_issues WHERE is_returned = 0 AND due_date < CURDATE()");
            if (rs != null && rs.next()) {
                overdueIssuesLabel.setText(String.valueOf(rs.getInt("count")));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Load user notifications
     */
    private void loadNotifications() {
        ObservableList<String> notifications = FXCollections.observableArrayList();
        
        try {
            // Get user's overdue books
            String overdueQuery = """
                SELECT b.title, bi.due_date 
                FROM book_issues bi 
                JOIN books b ON bi.book_id = b.book_id 
                WHERE bi.user_id = ? AND bi.is_returned = 0 AND bi.due_date < CURDATE()
                """;
            
            ResultSet rs = Database.executeQuery(overdueQuery, currentUser.getUserId());
            while (rs != null && rs.next()) {
                String title = rs.getString("title");
                LocalDate dueDate = rs.getDate("due_date").toLocalDate();
                notifications.add("OVERDUE: \"" + title + "\" was due on " + DateUtils.formatForDisplay(dueDate));
            }
            
            // Get books due soon
            String dueSoonQuery = """
                SELECT b.title, bi.due_date 
                FROM book_issues bi 
                JOIN books b ON bi.book_id = b.book_id 
                WHERE bi.user_id = ? AND bi.is_returned = 0 
                AND bi.due_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 2 DAY)
                """;
            
            rs = Database.executeQuery(dueSoonQuery, currentUser.getUserId());
            while (rs != null && rs.next()) {
                String title = rs.getString("title");
                LocalDate dueDate = rs.getDate("due_date").toLocalDate();
                notifications.add("DUE SOON: \"" + title + "\" is due on " + DateUtils.formatForDisplay(dueDate));
            }
            
            if (notifications.isEmpty()) {
                notifications.add("No notifications at this time.");
            }
            
            notificationsList.setItems(notifications);
            
        } catch (SQLException e) {
            e.printStackTrace();
            notifications.add("Error loading notifications.");
            notificationsList.setItems(notifications);
        }
    }
    
    /**
     * Open books management window
     */
    private void openBooksWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BookTable.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Books Management");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(Main.getPrimaryStage());
            
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            showError("Error opening books window: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Open users management window
     */
    private void openUsersWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserForm.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Users Management");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(Main.getPrimaryStage());
            
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            showError("Error opening users window: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Open issue/return window
     */
    private void openIssueReturnWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/IssueReturn.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Issue & Return Books");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(Main.getPrimaryStage());
            
            Scene scene = new Scene(root, 900, 650);
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            showError("Error opening issue/return window: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Open reports window
     */
    private void openReportsWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Report.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Reports");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(Main.getPrimaryStage());
            
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            showError("Error opening reports window: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}