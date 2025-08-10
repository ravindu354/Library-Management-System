package com.library.controllers;

import com.library.Database;
import com.library.models.Book;
import com.library.models.UserRole;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for books management
 * Handles book CRUD operations and search functionality
 */
public class BookController implements Initializable {
    
    @FXML private TableView<Book> booksTable;
    @FXML private TableColumn<Book, Integer> idColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> categoryColumn;
    @FXML private TableColumn<Book, String> isbnColumn;
    @FXML private TableColumn<Book, Integer> totalCopiesColumn;
    @FXML private TableColumn<Book, Integer> availableCopiesColumn;
    @FXML private TableColumn<Book, String> statusColumn;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> searchTypeCombo;
    @FXML private Button searchButton;
    @FXML private Button clearSearchButton;
    
    @FXML private Button addBookButton;
    @FXML private Button editBookButton;
    @FXML private Button deleteBookButton;
    @FXML private Button refreshButton;
    
    private ObservableList<Book> allBooks;
    private ObservableList<Book> filteredBooks;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupSearchControls();
        setupButtons();
        loadBooks();
        
        // Configure UI based on user role
        UserRole currentUserRole = LoginController.getCurrentUser().getRole();
        boolean canModify = (currentUserRole == UserRole.LIBRARIAN);
        
        addBookButton.setVisible(canModify);
        editBookButton.setVisible(canModify);
        deleteBookButton.setVisible(canModify);
    }
    
    /**
     * Setup table columns
     */
    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        totalCopiesColumn.setCellValueFactory(new PropertyValueFactory<>("totalCopies"));
        availableCopiesColumn.setCellValueFactory(new PropertyValueFactory<>("availableCopies"));
        
        // Custom cell factory for status column
        statusColumn.setCellValueFactory(cellData -> 
            cellData.getValue().availableCopiesProperty().asString().concat("/")
            .concat(cellData.getValue().totalCopiesProperty().asString())
        );
        
        // Initialize lists
        allBooks = FXCollections.observableArrayList();
        filteredBooks = FXCollections.observableArrayList();
        booksTable.setItems(filteredBooks);
    }
    
    /**
     * Setup search controls
     */
    private void setupSearchControls() {
        searchTypeCombo.setItems(FXCollections.observableArrayList(
            "Title", "Author", "Category", "ISBN"
        ));
        searchTypeCombo.setValue("Title");
        
        searchButton.setOnAction(e -> performSearch());
        clearSearchButton.setOnAction(e -> clearSearch());
        
        // Enable search on Enter key
        searchField.setOnAction(e -> performSearch());
    }
    
    /**
     * Setup button actions
     */
    private void setupButtons() {
        addBookButton.setOnAction(e -> addBook());
        editBookButton.setOnAction(e -> editBook());
        deleteBookButton.setOnAction(e -> deleteBook());
        refreshButton.setOnAction(e -> loadBooks());
    }
    
    /**
     * Load all books from database
     */
    private void loadBooks() {
        allBooks.clear();
        
        String query = """
            SELECT book_id, title, author, category, isbn, 
                   total_copies, available_copies, is_active
            FROM books 
            WHERE is_active = 1 
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
                allBooks.add(book);
            }
            
            // Show all books initially
            filteredBooks.setAll(allBooks);
            
        } catch (SQLException e) {
            showError("Error loading books: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Perform search based on search criteria
     */
    private void performSearch() {
        String searchText = searchField.getText().trim().toLowerCase();
        String searchType = searchTypeCombo.getValue();
        
        if (searchText.isEmpty()) {
            filteredBooks.setAll(allBooks);
            return;
        }
        
        filteredBooks.clear();
        
        for (Book book : allBooks) {
            boolean matches = switch (searchType) {
                case "Title" -> book.getTitle().toLowerCase().contains(searchText);
                case "Author" -> book.getAuthor().toLowerCase().contains(searchText);
                case "Category" -> book.getCategory().toLowerCase().contains(searchText);
                case "ISBN" -> book.getIsbn().toLowerCase().contains(searchText);
                default -> false;
            };
            
            if (matches) {
                filteredBooks.add(book);
            }
        }
    }
    
    /**
     * Clear search and show all books
     */
    private void clearSearch() {
        searchField.clear();
        filteredBooks.setAll(allBooks);
    }
    
    /**
     * Add new book
     */
    private void addBook() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BookForm.fxml"));
            Parent root = loader.load();
            
            BookFormController controller = loader.getController();
            controller.setBookController(this);
            
            Stage stage = new Stage();
            stage.setTitle("Add New Book");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(booksTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (Exception e) {
            showError("Error opening book form: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Edit selected book
     */
    private void editBook() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showWarning("Please select a book to edit.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/BookForm.fxml"));
            Parent root = loader.load();
            
            BookFormController controller = loader.getController();
            controller.setBookController(this);
            controller.setBook(selectedBook);
            
            Stage stage = new Stage();
            stage.setTitle("Edit Book");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(booksTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
        } catch (Exception e) {
            showError("Error opening book form: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Delete selected book
     */
    private void deleteBook() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showWarning("Please select a book to delete.");
            return;
        }
        
        // Check if book has active issues
        if (hasActiveIssues(selectedBook.getBookId())) {
            showWarning("Cannot delete book with active issues.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Book");
        alert.setHeaderText("Delete Book Confirmation");
        alert.setContentText("Are you sure you want to delete \"" + selectedBook.getTitle() + "\"?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (deleteBookFromDatabase(selectedBook.getBookId())) {
                loadBooks();
                showInfo("Book deleted successfully.");
            }
        }
    }
    
    /**
     * Check if book has active issues
     */
    private boolean hasActiveIssues(int bookId) {
        String query = "SELECT COUNT(*) as count FROM book_issues WHERE book_id = ? AND is_returned = 0";
        try {
            ResultSet rs = Database.executeQuery(query, bookId);
            return rs != null && rs.next() && rs.getInt("count") > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // Assume it has issues if there's an error
        }
    }
    
    /**
     * Delete book from database (soft delete)
     */
    private boolean deleteBookFromDatabase(int bookId) {
        String query = "UPDATE books SET is_active = 0 WHERE book_id = ?";
        return Database.executeUpdate(query, bookId) > 0;
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
    
    /**
     * Refresh books table
     */
    public void refreshBooks() {
        loadBooks();
    }
}