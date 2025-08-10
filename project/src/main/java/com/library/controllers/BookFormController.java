package com.library.controllers;

import com.library.Database;
import com.library.models.Book;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for book form (add/edit)
 * Handles book creation and modification
 */
public class BookFormController implements Initializable {
    
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField categoryField;
    @FXML private TextField isbnField;
    @FXML private Spinner<Integer> totalCopiesSpinner;
    @FXML private Spinner<Integer> availableCopiesSpinner;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label titleLabel;
    
    private Book currentBook;
    private BookController parentController;
    private boolean isEditMode = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupSpinners();
        setupButtons();
    }
    
    /**
     * Setup spinners
     */
    private void setupSpinners() {
        totalCopiesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        availableCopiesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 1));
        
        // Sync available copies with total copies
        totalCopiesSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isEditMode) {
                availableCopiesSpinner.getValueFactory().setValue(newVal);
            }
        });
    }
    
    /**
     * Setup button actions
     */
    private void setupButtons() {
        saveButton.setOnAction(e -> saveBook());
        cancelButton.setOnAction(e -> closeWindow());
    }
    
    /**
     * Set parent controller
     */
    public void setBookController(BookController controller) {
        this.parentController = controller;
    }
    
    /**
     * Set book for editing
     */
    public void setBook(Book book) {
        this.currentBook = book;
        this.isEditMode = true;
        
        titleLabel.setText("Edit Book");
        
        // Populate fields
        titleField.setText(book.getTitle());
        authorField.setText(book.getAuthor());
        categoryField.setText(book.getCategory());
        isbnField.setText(book.getIsbn());
        totalCopiesSpinner.getValueFactory().setValue(book.getTotalCopies());
        availableCopiesSpinner.getValueFactory().setValue(book.getAvailableCopies());
    }
    
    /**
     * Save book to database
     */
    private void saveBook() {
        // Validate input
        if (!validateInput()) {
            return;
        }
        
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String category = categoryField.getText().trim();
        String isbn = isbnField.getText().trim();
        int totalCopies = totalCopiesSpinner.getValue();
        int availableCopies = availableCopiesSpinner.getValue();
        
        boolean success;
        if (isEditMode) {
            success = updateBook(title, author, category, isbn, totalCopies, availableCopies);
        } else {
            success = createBook(title, author, category, isbn, totalCopies, availableCopies);
        }
        
        if (success) {
            parentController.refreshBooks();
            showInfo(isEditMode ? "Book updated successfully!" : "Book added successfully!");
            closeWindow();
        }
    }
    
    /**
     * Validate input fields
     */
    private boolean validateInput() {
        if (titleField.getText().trim().isEmpty()) {
            showError("Title is required.");
            titleField.requestFocus();
            return false;
        }
        
        if (authorField.getText().trim().isEmpty()) {
            showError("Author is required.");
            authorField.requestFocus();
            return false;
        }
        
        if (categoryField.getText().trim().isEmpty()) {
            showError("Category is required.");
            categoryField.requestFocus();
            return false;
        }
        
        if (isbnField.getText().trim().isEmpty()) {
            showError("ISBN is required.");
            isbnField.requestFocus();
            return false;
        }
        
        if (availableCopiesSpinner.getValue() > totalCopiesSpinner.getValue()) {
            showError("Available copies cannot exceed total copies.");
            return false;
        }
        
        // Check for duplicate ISBN (only for new books or when ISBN is changed)
        String isbn = isbnField.getText().trim();
        if (!isEditMode || !isbn.equals(currentBook.getIsbn())) {
            if (isIsbnExists(isbn)) {
                showError("A book with this ISBN already exists.");
                isbnField.requestFocus();
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check if ISBN already exists
     */
    private boolean isIsbnExists(String isbn) {
        String query = "SELECT COUNT(*) as count FROM books WHERE isbn = ? AND is_active = 1";
        try {
            var rs = Database.executeQuery(query, isbn);
            return rs != null && rs.next() && rs.getInt("count") > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Create new book
     */
    private boolean createBook(String title, String author, String category, String isbn, 
                              int totalCopies, int availableCopies) {
        String query = """
            INSERT INTO books (title, author, category, isbn, total_copies, available_copies, is_active)
            VALUES (?, ?, ?, ?, ?, ?, 1)
            """;
        
        return Database.executeUpdate(query, title, author, category, isbn, totalCopies, availableCopies) > 0;
    }
    
    /**
     * Update existing book
     */
    private boolean updateBook(String title, String author, String category, String isbn, 
                              int totalCopies, int availableCopies) {
        String query = """
            UPDATE books 
            SET title = ?, author = ?, category = ?, isbn = ?, 
                total_copies = ?, available_copies = ?
            WHERE book_id = ?
            """;
        
        return Database.executeUpdate(query, title, author, category, isbn, 
                                    totalCopies, availableCopies, currentBook.getBookId()) > 0;
    }
    
    /**
     * Close window
     */
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
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