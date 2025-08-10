package com.library.models;

import javafx.beans.property.*;

/**
 * Book model class representing library books
 * Contains all book information and availability status
 */
public class Book {
    
    private final IntegerProperty bookId;
    private final StringProperty title;
    private final StringProperty author;
    private final StringProperty category;
    private final StringProperty isbn;
    private final IntegerProperty totalCopies;
    private final IntegerProperty availableCopies;
    private final BooleanProperty isActive;
    
    // Default constructor
    public Book() {
        this.bookId = new SimpleIntegerProperty();
        this.title = new SimpleStringProperty("");
        this.author = new SimpleStringProperty("");
        this.category = new SimpleStringProperty("");
        this.isbn = new SimpleStringProperty("");
        this.totalCopies = new SimpleIntegerProperty(1);
        this.availableCopies = new SimpleIntegerProperty(1);
        this.isActive = new SimpleBooleanProperty(true);
    }
    
    // Constructor with parameters
    public Book(int bookId, String title, String author, String category, String isbn,
                int totalCopies, int availableCopies, boolean isActive) {
        this();
        setBookId(bookId);
        setTitle(title);
        setAuthor(author);
        setCategory(category);
        setIsbn(isbn);
        setTotalCopies(totalCopies);
        setAvailableCopies(availableCopies);
        setIsActive(isActive);
    }
    
    // Property getters
    public IntegerProperty bookIdProperty() { return bookId; }
    public StringProperty titleProperty() { return title; }
    public StringProperty authorProperty() { return author; }
    public StringProperty categoryProperty() { return category; }
    public StringProperty isbnProperty() { return isbn; }
    public IntegerProperty totalCopiesProperty() { return totalCopies; }
    public IntegerProperty availableCopiesProperty() { return availableCopies; }
    public BooleanProperty isActiveProperty() { return isActive; }
    
    // Getters
    public int getBookId() { return bookId.get(); }
    public String getTitle() { return title.get(); }
    public String getAuthor() { return author.get(); }
    public String getCategory() { return category.get(); }
    public String getIsbn() { return isbn.get(); }
    public int getTotalCopies() { return totalCopies.get(); }
    public int getAvailableCopies() { return availableCopies.get(); }
    public boolean getIsActive() { return isActive.get(); }
    
    // Setters
    public void setBookId(int bookId) { this.bookId.set(bookId); }
    public void setTitle(String title) { this.title.set(title); }
    public void setAuthor(String author) { this.author.set(author); }
    public void setCategory(String category) { this.category.set(category); }
    public void setIsbn(String isbn) { this.isbn.set(isbn); }
    public void setTotalCopies(int totalCopies) { this.totalCopies.set(totalCopies); }
    public void setAvailableCopies(int availableCopies) { this.availableCopies.set(availableCopies); }
    public void setIsActive(boolean isActive) { this.isActive.set(isActive); }
    
    // Utility methods
    public boolean isAvailable() {
        return availableCopies.get() > 0;
    }
    
    public String getAvailabilityStatus() {
        if (availableCopies.get() > 0) {
            return "Available (" + availableCopies.get() + "/" + totalCopies.get() + ")";
        } else {
            return "Not Available (0/" + totalCopies.get() + ")";
        }
    }
    
    @Override
    public String toString() {
        return title.get() + " by " + author.get();
    }
}