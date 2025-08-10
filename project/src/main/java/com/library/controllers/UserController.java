package com.library.controllers;

import com.library.Database;
import com.library.models.User;
import com.library.models.UserRole;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for user management
 * Handles user CRUD operations (Librarian only)
 */
public class UserController implements Initializable {
    
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, Boolean> activeColumn;
    
    // User form fields
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<UserRole> roleCombo;
    @FXML private CheckBox activeCheckBox;
    
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;
    @FXML private Button refreshButton;
    
    private ObservableList<User> users;
    private User selectedUser;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupForm();
        setupButtons();
        loadUsers();
    }
    
    /**
     * Setup table columns
     */
    private void setupTable() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().userIdProperty().asObject());
        usernameColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty()
                .concat(" ").concat(cellData.getValue().lastNameProperty()));
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        phoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneNumberProperty());
        roleColumn.setCellValueFactory(cellData -> cellData.getValue().roleProperty().asString());
        activeColumn.setCellValueFactory(cellData -> cellData.getValue().isActiveProperty());
        
        users = FXCollections.observableArrayList();
        usersTable.setItems(users);
        
        // Handle row selection
        usersTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedUser = newSelection;
                    populateForm(newSelection);
                }
            }
        );
    }
    
    /**
     * Setup form controls
     */
    private void setupForm() {
        roleCombo.setItems(FXCollections.observableArrayList(UserRole.values()));
        roleCombo.setValue(UserRole.STUDENT);
        activeCheckBox.setSelected(true);
    }
    
    /**
     * Setup button actions
     */
    private void setupButtons() {
        addButton.setOnAction(e -> addUser());
        updateButton.setOnAction(e -> updateUser());
        deleteButton.setOnAction(e -> deleteUser());
        clearButton.setOnAction(e -> clearForm());
        refreshButton.setOnAction(e -> loadUsers());
    }
    
    /**
     * Load all users from database
     */
    private void loadUsers() {
        users.clear();
        
        String query = """
            SELECT user_id, username, password, first_name, last_name, 
                   email, phone_number, role, is_active
            FROM users 
            ORDER BY first_name, last_name
            """;
        
        try {
            ResultSet rs = Database.executeQuery(query);
            while (rs != null && rs.next()) {
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("phone_number"),
                    UserRole.fromString(rs.getString("role")),
                    rs.getBoolean("is_active")
                );
                users.add(user);
            }
            
        } catch (SQLException e) {
            showError("Error loading users: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Populate form with selected user data
     */
    private void populateForm(User user) {
        usernameField.setText(user.getUsername());
        passwordField.setText(""); // Don't show password
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhoneNumber());
        roleCombo.setValue(user.getRole());
        activeCheckBox.setSelected(user.getIsActive());
    }
    
    /**
     * Clear form fields
     */
    private void clearForm() {
        selectedUser = null;
        usernameField.clear();
        passwordField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        roleCombo.setValue(UserRole.STUDENT);
        activeCheckBox.setSelected(true);
        usersTable.getSelectionModel().clearSelection();
    }
    
    /**
     * Add new user
     */
    private void addUser() {
        if (!validateInput()) {
            return;
        }
        
        // Check if username already exists
        if (isUsernameExists(usernameField.getText().trim())) {
            showError("Username already exists. Please choose a different username.");
            return;
        }
        
        String query = """
            INSERT INTO users (username, password, first_name, last_name, 
                             email, phone_number, role, is_active)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        int result = Database.executeUpdate(query,
            usernameField.getText().trim(),
            passwordField.getText(), // Note: In practice, this should be hashed
            firstNameField.getText().trim(),
            lastNameField.getText().trim(),
            emailField.getText().trim(),
            phoneField.getText().trim(),
            roleCombo.getValue().getDisplayName(),
            activeCheckBox.isSelected()
        );
        
        if (result > 0) {
            showInfo("User added successfully!");
            clearForm();
            loadUsers();
        } else {
            showError("Error adding user.");
        }
    }
    
    /**
     * Update selected user
     */
    private void updateUser() {
        if (selectedUser == null) {
            showWarning("Please select a user to update.");
            return;
        }
        
        if (!validateInput()) {
            return;
        }
        
        // Check if username already exists (excluding current user)
        if (!usernameField.getText().trim().equals(selectedUser.getUsername()) &&
            isUsernameExists(usernameField.getText().trim())) {
            showError("Username already exists. Please choose a different username.");
            return;
        }
        
        String query = """
            UPDATE users 
            SET username = ?, first_name = ?, last_name = ?, 
                email = ?, phone_number = ?, role = ?, is_active = ?
            WHERE user_id = ?
            """;
        
        // Update password only if provided
        if (!passwordField.getText().isEmpty()) {
            query = """
                UPDATE users 
                SET username = ?, password = ?, first_name = ?, last_name = ?, 
                    email = ?, phone_number = ?, role = ?, is_active = ?
                WHERE user_id = ?
                """;
            
            int result = Database.executeUpdate(query,
                usernameField.getText().trim(),
                passwordField.getText(),
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                roleCombo.getValue().getDisplayName(),
                activeCheckBox.isSelected(),
                selectedUser.getUserId()
            );
            
            if (result > 0) {
                showInfo("User updated successfully!");
                clearForm();
                loadUsers();
            } else {
                showError("Error updating user.");
            }
        } else {
            int result = Database.executeUpdate(query,
                usernameField.getText().trim(),
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                roleCombo.getValue().getDisplayName(),
                activeCheckBox.isSelected(),
                selectedUser.getUserId()
            );
            
            if (result > 0) {
                showInfo("User updated successfully!");
                clearForm();
                loadUsers();
            } else {
                showError("Error updating user.");
            }
        }
    }
    
    /**
     * Delete selected user
     */
    private void deleteUser() {
        if (selectedUser == null) {
            showWarning("Please select a user to delete.");
            return;
        }
        
        // Check if user has active book issues
        if (hasActiveIssues(selectedUser.getUserId())) {
            showWarning("Cannot delete user with active book issues.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Delete User Confirmation");
        alert.setContentText("Are you sure you want to delete user \"" + selectedUser.getFullName() + "\"?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String query = "UPDATE users SET is_active = 0 WHERE user_id = ?";
            if (Database.executeUpdate(query, selectedUser.getUserId()) > 0) {
                showInfo("User deleted successfully!");
                clearForm();
                loadUsers();
            } else {
                showError("Error deleting user.");
            }
        }
    }
    
    /**
     * Check if user has active book issues
     */
    private boolean hasActiveIssues(int userId) {
        String query = "SELECT COUNT(*) as count FROM book_issues WHERE user_id = ? AND is_returned = 0";
        try {
            ResultSet rs = Database.executeQuery(query, userId);
            return rs != null && rs.next() && rs.getInt("count") > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // Assume user has issues if there's an error
        }
    }
    
    /**
     * Check if username already exists
     */
    private boolean isUsernameExists(String username) {
        String query = "SELECT COUNT(*) as count FROM users WHERE username = ?";
        try {
            ResultSet rs = Database.executeQuery(query, username);
            return rs != null && rs.next() && rs.getInt("count") > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Validate input fields
     */
    private boolean validateInput() {
        if (usernameField.getText().trim().isEmpty()) {
            showError("Username is required.");
            usernameField.requestFocus();
            return false;
        }
        
        if (selectedUser == null && passwordField.getText().isEmpty()) {
            showError("Password is required for new users.");
            passwordField.requestFocus();
            return false;
        }
        
        if (firstNameField.getText().trim().isEmpty()) {
            showError("First name is required.");
            firstNameField.requestFocus();
            return false;
        }
        
        if (lastNameField.getText().trim().isEmpty()) {
            showError("Last name is required.");
            lastNameField.requestFocus();
            return false;
        }
        
        if (emailField.getText().trim().isEmpty()) {
            showError("Email is required.");
            emailField.requestFocus();
            return false;
        }
        
        // Basic email validation
        String email = emailField.getText().trim();
        if (!email.contains("@") || !email.contains(".")) {
            showError("Please enter a valid email address.");
            emailField.requestFocus();
            return false;
        }
        
        return true;
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
     * Show warning alert
     */
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
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
}