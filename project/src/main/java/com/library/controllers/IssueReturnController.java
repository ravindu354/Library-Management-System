package com.library.controllers;

import com.library.Database;
import com.library.models.Book;
import com.library.models.Issue;
import com.library.models.User;
import com.library.models.UserRole;
import com.library.utils.DateUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Controller for book issue and return operations
 * Handles borrowing and returning books with fine calculations
 */
public class IssueReturnController implements Initializable {
    
    // Issue Book Tab
    @FXML private Tab issueTab;
    @FXML private ComboBox<User> userComboIssue;
    @FXML private ComboBox<Book> bookComboIssue;
    @FXML private DatePicker issueDatePicker;
    @FXML private DatePicker dueDatePicker;
    @FXML private Button issueBookButton;
    
    // Return Book Tab
    @FXML private Tab returnTab;
    @FXML private ComboBox<User> userComboReturn;
    @FXML private ComboBox<Issue> issueComboReturn;
    @FXML private DatePicker returnDatePicker;
    @FXML private Label fineLabel;
    @FXML private Button returnBookButton;
    @FXML private Button calculateFineButton;
    
    // Active Issues Table
    @FXML private TableView<Issue> activeIssuesTable;
    @FXML private TableColumn<Issue, Integer> issueIdColumn;
    @FXML private TableColumn<Issue, String> bookTitleColumn;
    @FXML private TableColumn<Issue, String> userNameColumn;
    @FXML private TableColumn<Issue, String> issueDateColumn;
    @FXML private TableColumn<Issue, String> dueDateColumn;
    @FXML private TableColumn<Issue, String> statusColumn;
    
    @FXML private Button refreshButton;
    
    private ObservableList<User> users;
    private ObservableList<Book> availableBooks;
    private ObservableList<Issue> userIssues;
    private ObservableList<Issue> activeIssues;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupControls();
        setupTable();
        loadData();
        setupButtons();
        
        // Set default dates
        issueDatePicker.setValue(LocalDate.now());
        dueDatePicker.setValue(DateUtils.calculateDueDate(LocalDate.now()));
        returnDatePicker.setValue(LocalDate.now());
    }
    
    /**
     * Setup form controls
     */
    private void setupControls() {
        users = FXCollections.observableArrayList();
        availableBooks = FXCollections.observableArrayList();
        userIssues = FXCollections.observableArrayList();
        activeIssues = FXCollections.observableArrayList();
        
        userComboIssue.setItems(users);
        bookComboIssue.setItems(availableBooks);
        userComboReturn.setItems(users);
        issueComboReturn.setItems(userIssues);
        
        // Auto-calculate due date when issue date changes
        issueDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                dueDatePicker.setValue(DateUtils.calculateDueDate(newDate));
            }
        });
        
        // Load user's active issues when user is selected for return
        userComboReturn.valueProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null) {
                loadUserActiveIssues(newUser.getUserId());
            }
        });
        
        // Calculate fine when return date or issue is changed
        returnDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> calculateFine());
        issueComboReturn.valueProperty().addListener((obs, oldIssue, newIssue) -> calculateFine());
    }
    
    /**
     * Setup active issues table
     */
    private void setupTable() {
        issueIdColumn.setCellValueFactory(new PropertyValueFactory<>("issueId"));
        bookTitleColumn.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("userFullName"));
        
        issueDateColumn.setCellValueFactory(cellData -> 
            cellData.getValue().issueDateProperty().asString()
        );
        dueDateColumn.setCellValueFactory(cellData -> 
            cellData.getValue().dueDateProperty().asString()
        );
        statusColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getStatus(),
                cellData.getValue().dueDateProperty(),
                cellData.getValue().isReturnedProperty()
            )
        );
        
        activeIssuesTable.setItems(activeIssues);
    }
    
    /**
     * Setup button actions
     */
    private void setupButtons() {
        issueBookButton.setOnAction(e -> issueBook());
        returnBookButton.setOnAction(e -> returnBook());
        calculateFineButton.setOnAction(e -> calculateFine());
        refreshButton.setOnAction(e -> loadData());
    }
    
    /**
     * Load all necessary data
     */
    private void loadData() {
        loadUsers();
        loadAvailableBooks();
        loadActiveIssues();
    }
    
    /**
     * Load all active users
     */
    private void loadUsers() {
        users.clear();
        
        String query = """
            SELECT user_id, username, first_name, last_name, email, 
                   phone_number, role, is_active
            FROM users 
            WHERE is_active = 1 
            ORDER BY first_name, last_name
            """;
        
        try {
            ResultSet rs = Database.executeQuery(query);
            while (rs != null && rs.next()) {
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    "", // Don't load password
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
     * Load available books (books with available copies > 0)
     */
    private void loadAvailableBooks() {
        availableBooks.clear();
        
        String query = """
            SELECT book_id, title, author, category, isbn, 
                   total_copies, available_copies, is_active
            FROM books 
            WHERE is_active = 1 AND available_copies > 0
            ORDER BY title
            """;
        
        try {
            ResultSet rs = Database.executeQuery(query);
            while (rs != null && rs.next()) {
                Book book = new Book(
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("category"),
                    rs.getString("isbn"),
                    rs.getInt("total_copies"),
                    rs.getInt("available_copies"),
                    rs.getBoolean("is_active")
                );
                availableBooks.add(book);
            }
        } catch (SQLException e) {
            showError("Error loading available books: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load active issues for display
     */
    private void loadActiveIssues() {
        activeIssues.clear();
        
        String query = """
            SELECT bi.issue_id, bi.book_id, bi.user_id, bi.issue_date, 
                   bi.due_date, bi.return_date, bi.fine_amount, bi.is_returned,
                   b.title as book_title, 
                   CONCAT(u.first_name, ' ', u.last_name) as user_name
            FROM book_issues bi
            JOIN books b ON bi.book_id = b.book_id
            JOIN users u ON bi.user_id = u.user_id
            WHERE bi.is_returned = 0
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
                activeIssues.add(issue);
            }
        } catch (SQLException e) {
            showError("Error loading active issues: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load active issues for a specific user
     */
    private void loadUserActiveIssues(int userId) {
        userIssues.clear();
        
        String query = """
            SELECT bi.issue_id, bi.book_id, bi.user_id, bi.issue_date, 
                   bi.due_date, bi.return_date, bi.fine_amount, bi.is_returned,
                   b.title as book_title
            FROM book_issues bi
            JOIN books b ON bi.book_id = b.book_id
            WHERE bi.user_id = ? AND bi.is_returned = 0
            ORDER BY bi.due_date
            """;
        
        try {
            ResultSet rs = Database.executeQuery(query, userId);
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
                userIssues.add(issue);
            }
        } catch (SQLException e) {
            showError("Error loading user issues: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Issue a book to a user
     */
    private void issueBook() {
        // Validate inputs
        User selectedUser = userComboIssue.getValue();
        Book selectedBook = bookComboIssue.getValue();
        LocalDate issueDate = issueDatePicker.getValue();
        LocalDate dueDate = dueDatePicker.getValue();
        
        if (selectedUser == null) {
            showError("Please select a user.");
            return;
        }
        
        if (selectedBook == null) {
            showError("Please select a book.");
            return;
        }
        
        if (issueDate == null || dueDate == null) {
            showError("Please select issue and due dates.");
            return;
        }
        
        if (issueDate.isAfter(dueDate)) {
            showError("Issue date cannot be after due date.");
            return;
        }
        
        // Check if book is still available
        if (!isBookAvailable(selectedBook.getBookId())) {
            showError("This book is no longer available.");
            return;
        }
        
        // Issue the book
        if (createIssueRecord(selectedUser.getUserId(), selectedBook.getBookId(), issueDate, dueDate)) {
            // Update book availability
            updateBookAvailability(selectedBook.getBookId(), -1);
            
            showInfo("Book issued successfully!");
            clearIssueForm();
            loadData();
        } else {
            showError("Error issuing book.");
        }
    }
    
    /**
     * Return a book
     */
    private void returnBook() {
        Issue selectedIssue = issueComboReturn.getValue();
        LocalDate returnDate = returnDatePicker.getValue();
        
        if (selectedIssue == null) {
            showError("Please select an issue to return.");
            return;
        }
        
        if (returnDate == null) {
            showError("Please select return date.");
            return;
        }
        
        if (returnDate.isBefore(selectedIssue.getIssueDate())) {
            showError("Return date cannot be before issue date.");
            return;
        }
        
        // Calculate fine
        double fine = DateUtils.calculateFine(selectedIssue.getDueDate(), returnDate);
        
        // Update issue record
        if (updateIssueReturn(selectedIssue.getIssueId(), returnDate, fine)) {
            // Update book availability
            updateBookAvailability(selectedIssue.getBookId(), 1);
            
            if (fine > 0) {
                showInfo(String.format("Book returned successfully!\nFine amount: $%.2f", fine));
            } else {
                showInfo("Book returned successfully!");
            }
            
            clearReturnForm();
            loadData();
        } else {
            showError("Error returning book.");
        }
    }
    
    /**
     * Calculate and display fine
     */
    private void calculateFine() {
        Issue selectedIssue = issueComboReturn.getValue();
        LocalDate returnDate = returnDatePicker.getValue();
        
        if (selectedIssue != null && returnDate != null) {
            double fine = DateUtils.calculateFine(selectedIssue.getDueDate(), returnDate);
            fineLabel.setText(String.format("Fine: $%.2f", fine));
        } else {
            fineLabel.setText("Fine: $0.00");
        }
    }
    
    /**
     * Check if book is available
     */
    private boolean isBookAvailable(int bookId) {
        String query = "SELECT available_copies FROM books WHERE book_id = ? AND is_active = 1";
        try {
            ResultSet rs = Database.executeQuery(query, bookId);
            return rs != null && rs.next() && rs.getInt("available_copies") > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Create issue record in database
     */
    private boolean createIssueRecord(int userId, int bookId, LocalDate issueDate, LocalDate dueDate) {
        String query = """
            INSERT INTO book_issues (user_id, book_id, issue_date, due_date, is_returned, fine_amount)
            VALUES (?, ?, ?, ?, 0, 0.0)
            """;
        
        return Database.executeUpdate(query, userId, bookId, issueDate, dueDate) > 0;
    }
    
    /**
     * Update issue record for return
     */
    private boolean updateIssueReturn(int issueId, LocalDate returnDate, double fine) {
        String query = """
            UPDATE book_issues 
            SET return_date = ?, fine_amount = ?, is_returned = 1
            WHERE issue_id = ?
            """;
        
        return Database.executeUpdate(query, returnDate, fine, issueId) > 0;
    }
    
    /**
     * Update book availability
     */
    private boolean updateBookAvailability(int bookId, int change) {
        String query = "UPDATE books SET available_copies = available_copies + ? WHERE book_id = ?";
        return Database.executeUpdate(query, change, bookId) > 0;
    }
    
    /**
     * Clear issue form
     */
    private void clearIssueForm() {
        userComboIssue.setValue(null);
        bookComboIssue.setValue(null);
        issueDatePicker.setValue(LocalDate.now());
        dueDatePicker.setValue(DateUtils.calculateDueDate(LocalDate.now()));
    }
    
    /**
     * Clear return form
     */
    private void clearReturnForm() {
        userComboReturn.setValue(null);
        issueComboReturn.setValue(null);
        returnDatePicker.setValue(LocalDate.now());
        fineLabel.setText("Fine: $0.00");
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
}