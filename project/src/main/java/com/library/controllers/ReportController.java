package com.library.controllers;

import com.library.Database;
import com.library.models.Issue;
import com.library.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Controller for reports generation
 * Provides various library reports with export functionality
 */
public class ReportController implements Initializable {
    
    @FXML private TabPane reportTabPane;
    
    // All Issues Report
    @FXML private Tab allIssuesTab;
    @FXML private TableView<Issue> allIssuesTable;
    @FXML private TableColumn<Issue, Integer> allIssueIdColumn;
    @FXML private TableColumn<Issue, String> allBookTitleColumn;
    @FXML private TableColumn<Issue, String> allUserNameColumn;
    @FXML private TableColumn<Issue, String> allIssueDateColumn;
    @FXML private TableColumn<Issue, String> allDueDateColumn;
    @FXML private TableColumn<Issue, String> allReturnDateColumn;
    @FXML private TableColumn<Issue, Double> allFineColumn;
    @FXML private TableColumn<Issue, String> allStatusColumn;
    @FXML private Button exportAllIssuesButton;
    @FXML private Label allIssuesCountLabel;
    
    // Overdue Books Report
    @FXML private Tab overdueTab;
    @FXML private TableView<Issue> overdueTable;
    @FXML private TableColumn<Issue, Integer> overdueIssueIdColumn;
    @FXML private TableColumn<Issue, String> overdueBookTitleColumn;
    @FXML private TableColumn<Issue, String> overdueUserNameColumn;
    @FXML private TableColumn<Issue, String> overdueDueDateColumn;
    @FXML private TableColumn<Issue, Long> overdueDaysColumn;
    @FXML private TableColumn<Issue, Double> overdueFineColumn;
    @FXML private Button exportOverdueButton;
    @FXML private Label overdueCountLabel;
    
    // Active Users Report
    @FXML private Tab activeUsersTab;
    @FXML private TableView<UserActivity> activeUsersTable;
    @FXML private TableColumn<UserActivity, String> userNameColumn;
    @FXML private TableColumn<UserActivity, String> userRoleColumn;
    @FXML private TableColumn<UserActivity, Integer> totalIssuesColumn;
    @FXML private TableColumn<UserActivity, Integer> activeIssuesColumn;
    @FXML private TableColumn<UserActivity, Double> totalFinesColumn;
    @FXML private Button exportActiveUsersButton;
    @FXML private Label activeUsersCountLabel;
    
    @FXML private Button refreshAllButton;
    
    private ObservableList<Issue> allIssues;
    private ObservableList<Issue> overdueIssues;
    private ObservableList<UserActivity> activeUsers;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTables();
        setupButtons();
        loadAllReports();
    }
    
    /**
     * Setup all table columns
     */
    private void setupTables() {
        setupAllIssuesTable();
        setupOverdueTable();
        setupActiveUsersTable();
        
        allIssues = FXCollections.observableArrayList();
        overdueIssues = FXCollections.observableArrayList();
        activeUsers = FXCollections.observableArrayList();
        
        allIssuesTable.setItems(allIssues);
        overdueTable.setItems(overdueIssues);
        activeUsersTable.setItems(activeUsers);
    }
    
    /**
     * Setup all issues table
     */
    private void setupAllIssuesTable() {
        allIssueIdColumn.setCellValueFactory(new PropertyValueFactory<>("issueId"));
        allBookTitleColumn.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        allUserNameColumn.setCellValueFactory(new PropertyValueFactory<>("userFullName"));
        allIssueDateColumn.setCellValueFactory(cellData -> 
            cellData.getValue().issueDateProperty().asString());
        allDueDateColumn.setCellValueFactory(cellData -> 
            cellData.getValue().dueDateProperty().asString());
        allReturnDateColumn.setCellValueFactory(cellData -> 
            cellData.getValue().returnDateProperty().asString());
        allFineColumn.setCellValueFactory(new PropertyValueFactory<>("fineAmount"));
        allStatusColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getStatus(),
                cellData.getValue().isReturnedProperty()));
    }
    
    /**
     * Setup overdue table
     */
    private void setupOverdueTable() {
        overdueIssueIdColumn.setCellValueFactory(new PropertyValueFactory<>("issueId"));
        overdueBookTitleColumn.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        overdueUserNameColumn.setCellValueFactory(new PropertyValueFactory<>("userFullName"));
        overdueDueDateColumn.setCellValueFactory(cellData -> 
            cellData.getValue().dueDateProperty().asString());
        
        // Days overdue calculation
        overdueDaysColumn.setCellValueFactory(cellData -> {
            Issue issue = cellData.getValue();
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                issue.getDueDate(), LocalDate.now());
            return javafx.beans.binding.Bindings.createObjectBinding(
                () -> daysOverdue, issue.dueDateProperty());
        });
        
        overdueFineColumn.setCellValueFactory(cellData -> {
            Issue issue = cellData.getValue();
            double currentFine = com.library.utils.DateUtils.calculateFine(
                issue.getDueDate(), LocalDate.now());
            return javafx.beans.binding.Bindings.createObjectBinding(
                () -> currentFine, issue.dueDateProperty());
        });
    }
    
    /**
     * Setup active users table
     */
    private void setupActiveUsersTable() {
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        userRoleColumn.setCellValueFactory(new PropertyValueFactory<>("userRole"));
        totalIssuesColumn.setCellValueFactory(new PropertyValueFactory<>("totalIssues"));
        activeIssuesColumn.setCellValueFactory(new PropertyValueFactory<>("activeIssues"));
        totalFinesColumn.setCellValueFactory(new PropertyValueFactory<>("totalFines"));
    }
    
    /**
     * Setup button actions
     */
    private void setupButtons() {
        exportAllIssuesButton.setOnAction(e -> exportAllIssues());
        exportOverdueButton.setOnAction(e -> exportOverdueIssues());
        exportActiveUsersButton.setOnAction(e -> exportActiveUsers());
        refreshAllButton.setOnAction(e -> loadAllReports());
    }
    
    /**
     * Load all reports
     */
    private void loadAllReports() {
        loadAllIssuesReport();
        loadOverdueReport();
        loadActiveUsersReport();
    }
    
    /**
     * Load all issues report
     */
    private void loadAllIssuesReport() {
        allIssues.clear();
        
        String query = """
            SELECT bi.issue_id, bi.book_id, bi.user_id, bi.issue_date, 
                   bi.due_date, bi.return_date, bi.fine_amount, bi.is_returned,
                   b.title as book_title, 
                   CONCAT(u.first_name, ' ', u.last_name) as user_name
            FROM book_issues bi
            JOIN books b ON bi.book_id = b.book_id
            JOIN users u ON bi.user_id = u.user_id
            ORDER BY bi.issue_date DESC
            """;
        
        try {
            ResultSet rs = Database.executeQuery(query);
            while (rs != null && rs.next()) {
                Issue issue = new Issue(
                    rs.getInt("issue_id"),
                    rs.getInt("book_id"),
                    rs.getInt("user_id"),
                    rs.getDate("issue_date").toLocalDate(),
                    rs.getDate("due_date").toLocalDate(),
                    rs.getDate("return_date") != null ? rs.getDate("return_date").toLocalDate() : null,
                    rs.getDouble("fine_amount"),
                    rs.getBoolean("is_returned")
                );
                issue.setBookTitle(rs.getString("book_title"));
                issue.setUserFullName(rs.getString("user_name"));
                allIssues.add(issue);
            }
            
            allIssuesCountLabel.setText("Total Issues: " + allIssues.size());
            
        } catch (SQLException e) {
            showError("Error loading all issues report: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load overdue books report
     */
    private void loadOverdueReport() {
        overdueIssues.clear();
        
        String query = """
            SELECT bi.issue_id, bi.book_id, bi.user_id, bi.issue_date, 
                   bi.due_date, bi.return_date, bi.fine_amount, bi.is_returned,
                   b.title as book_title, 
                   CONCAT(u.first_name, ' ', u.last_name) as user_name
            FROM book_issues bi
            JOIN books b ON bi.book_id = b.book_id
            JOIN users u ON bi.user_id = u.user_id
            WHERE bi.is_returned = 0 AND bi.due_date < CURDATE()
            ORDER BY bi.due_date
            """;
        
        try {
            ResultSet rs = Database.executeQuery(query);
            while (rs != null && rs.next()) {
                Issue issue = new Issue(
                    rs.getInt("issue_id"),
                    rs.getInt("book_id"),
                    rs.getInt("user_id"),
                    rs.getDate("issue_date").toLocalDate(),
                    rs.getDate("due_date").toLocalDate(),
                    rs.getDate("return_date") != null ? rs.getDate("return_date").toLocalDate() : null,
                    rs.getDouble("fine_amount"),
                    rs.getBoolean("is_returned")
                );
                issue.setBookTitle(rs.getString("book_title"));
                issue.setUserFullName(rs.getString("user_name"));
                overdueIssues.add(issue);
            }
            
            overdueCountLabel.setText("Overdue Books: " + overdueIssues.size());
            
        } catch (SQLException e) {
            showError("Error loading overdue report: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load active users report
     */
    private void loadActiveUsersReport() {
        activeUsers.clear();
        
        String query = """
            SELECT u.user_id, CONCAT(u.first_name, ' ', u.last_name) as user_name, 
                   u.role,
                   COUNT(bi.issue_id) as total_issues,
                   COUNT(CASE WHEN bi.is_returned = 0 THEN 1 END) as active_issues,
                   COALESCE(SUM(bi.fine_amount), 0) as total_fines
            FROM users u
            LEFT JOIN book_issues bi ON u.user_id = bi.user_id
            WHERE u.is_active = 1
            GROUP BY u.user_id, u.first_name, u.last_name, u.role
            HAVING COUNT(bi.issue_id) > 0
            ORDER BY total_issues DESC
            """;
        
        try {
            ResultSet rs = Database.executeQuery(query);
            while (rs != null && rs.next()) {
                UserActivity userActivity = new UserActivity(
                    rs.getString("user_name"),
                    rs.getString("role"),
                    rs.getInt("total_issues"),
                    rs.getInt("active_issues"),
                    rs.getDouble("total_fines")
                );
                activeUsers.add(userActivity);
            }
            
            activeUsersCountLabel.setText("Active Users: " + activeUsers.size());
            
        } catch (SQLException e) {
            showError("Error loading active users report: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Export all issues to CSV
     */
    private void exportAllIssues() {
        exportToCSV(allIssues, "All_Issues_Report", 
            "Issue ID,Book Title,User Name,Issue Date,Due Date,Return Date,Fine Amount,Status");
    }
    
    /**
     * Export overdue issues to CSV
     */
    private void exportOverdueIssues() {
        exportToCSV(overdueIssues, "Overdue_Books_Report", 
            "Issue ID,Book Title,User Name,Due Date,Days Overdue,Current Fine");
    }
    
    /**
     * Export active users to CSV
     */
    private void exportActiveUsers() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Active Users Report");
        fileChooser.setInitialFileName("Active_Users_Report.csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        File file = fileChooser.showSaveDialog(activeUsersTable.getScene().getWindow());
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Write header
                writer.write("User Name,Role,Total Issues,Active Issues,Total Fines");
                writer.newLine();
                
                // Write data
                for (UserActivity user : activeUsers) {
                    writer.write(String.format("%s,%s,%d,%d,%.2f",
                        user.getUserName(),
                        user.getUserRole(),
                        user.getTotalIssues(),
                        user.getActiveIssues(),
                        user.getTotalFines()));
                    writer.newLine();
                }
                
                showInfo("Active users report exported successfully to " + file.getName());
                
            } catch (IOException e) {
                showError("Error exporting active users report: " + e.getMessage());
            }
        }
    }
    
    /**
     * Generic method to export issues to CSV
     */
    private void exportToCSV(ObservableList<Issue> issues, String fileName, String header) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export " + fileName);
        fileChooser.setInitialFileName(fileName + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        File file = fileChooser.showSaveDialog(allIssuesTable.getScene().getWindow());
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // Write header
                writer.write(header);
                writer.newLine();
                
                // Write data
                for (Issue issue : issues) {
                    if (fileName.contains("Overdue")) {
                        long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                            issue.getDueDate(), LocalDate.now());
                        double currentFine = com.library.utils.DateUtils.calculateFine(
                            issue.getDueDate(), LocalDate.now());
                        
                        writer.write(String.format("%d,%s,%s,%s,%d,%.2f",
                            issue.getIssueId(),
                            issue.getBookTitle(),
                            issue.getUserFullName(),
                            issue.getDueDate().toString(),
                            daysOverdue,
                            currentFine));
                    } else {
                        writer.write(String.format("%d,%s,%s,%s,%s,%s,%.2f,%s",
                            issue.getIssueId(),
                            issue.getBookTitle(),
                            issue.getUserFullName(),
                            issue.getIssueDate().toString(),
                            issue.getDueDate().toString(),
                            issue.getReturnDate() != null ? issue.getReturnDate().toString() : "",
                            issue.getFineAmount(),
                            issue.getStatus()));
                    }
                    writer.newLine();
                }
                
                showInfo("Report exported successfully to " + file.getName());
                
            } catch (IOException e) {
                showError("Error exporting report: " + e.getMessage());
            }
        }
    }
    
    /**
     * Show error alert
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show info alert
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Inner class for user activity data
     */
    public static class UserActivity {
        private final String userName;
        private final String userRole;
        private final int totalIssues;
        private final int activeIssues;
        private final double totalFines;
        
        public UserActivity(String userName, String userRole, int totalIssues, 
                           int activeIssues, double totalFines) {
            this.userName = userName;
            this.userRole = userRole;
            this.totalIssues = totalIssues;
            this.activeIssues = activeIssues;
            this.totalFines = totalFines;
        }
        
        public String getUserName() { return userName; }
        public String getUserRole() { return userRole; }
        public int getTotalIssues() { return totalIssues; }
        public int getActiveIssues() { return activeIssues; }
        public double getTotalFines() { return totalFines; }
    }
}