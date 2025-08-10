package com.library.models;

import com.library.utils.DateUtils;
import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * Issue model class representing book issuance records
 * Tracks book borrowing, returns, and fines
 */
public class Issue {
    
    private final IntegerProperty issueId;
    private final IntegerProperty bookId;
    private final IntegerProperty userId;
    private final ObjectProperty<LocalDate> issueDate;
    private final ObjectProperty<LocalDate> dueDate;
    private final ObjectProperty<LocalDate> returnDate;
    private final DoubleProperty fineAmount;
    private final BooleanProperty isReturned;
    
    // For display purposes
    private final StringProperty bookTitle;
    private final StringProperty userFullName;
    
    // Default constructor
    public Issue() {
        this.issueId = new SimpleIntegerProperty();
        this.bookId = new SimpleIntegerProperty();
        this.userId = new SimpleIntegerProperty();
        this.issueDate = new SimpleObjectProperty<>();
        this.dueDate = new SimpleObjectProperty<>();
        this.returnDate = new SimpleObjectProperty<>();
        this.fineAmount = new SimpleDoubleProperty(0.0);
        this.isReturned = new SimpleBooleanProperty(false);
        this.bookTitle = new SimpleStringProperty("");
        this.userFullName = new SimpleStringProperty("");
    }
    
    // Constructor with parameters
    public Issue(int issueId, int bookId, int userId, LocalDate issueDate, 
                 LocalDate dueDate, LocalDate returnDate, double fineAmount, boolean isReturned) {
        this();
        setIssueId(issueId);
        setBookId(bookId);
        setUserId(userId);
        setIssueDate(issueDate);
        setDueDate(dueDate);
        setReturnDate(returnDate);
        setFineAmount(fineAmount);
        setIsReturned(isReturned);
    }
    
    // Property getters
    public IntegerProperty issueIdProperty() { return issueId; }
    public IntegerProperty bookIdProperty() { return bookId; }
    public IntegerProperty userIdProperty() { return userId; }
    public ObjectProperty<LocalDate> issueDateProperty() { return issueDate; }
    public ObjectProperty<LocalDate> dueDateProperty() { return dueDate; }
    public ObjectProperty<LocalDate> returnDateProperty() { return returnDate; }
    public DoubleProperty fineAmountProperty() { return fineAmount; }
    public BooleanProperty isReturnedProperty() { return isReturned; }
    public StringProperty bookTitleProperty() { return bookTitle; }
    public StringProperty userFullNameProperty() { return userFullName; }
    
    // Getters
    public int getIssueId() { return issueId.get(); }
    public int getBookId() { return bookId.get(); }
    public int getUserId() { return userId.get(); }
    public LocalDate getIssueDate() { return issueDate.get(); }
    public LocalDate getDueDate() { return dueDate.get(); }
    public LocalDate getReturnDate() { return returnDate.get(); }
    public double getFineAmount() { return fineAmount.get(); }
    public boolean getIsReturned() { return isReturned.get(); }
    public String getBookTitle() { return bookTitle.get(); }
    public String getUserFullName() { return userFullName.get(); }
    
    // Setters
    public void setIssueId(int issueId) { this.issueId.set(issueId); }
    public void setBookId(int bookId) { this.bookId.set(bookId); }
    public void setUserId(int userId) { this.userId.set(userId); }
    public void setIssueDate(LocalDate issueDate) { 
        this.issueDate.set(issueDate); 
        if (issueDate != null && dueDate.get() == null) {
            setDueDate(DateUtils.calculateDueDate(issueDate));
        }
    }
    public void setDueDate(LocalDate dueDate) { this.dueDate.set(dueDate); }
    public void setReturnDate(LocalDate returnDate) { 
        this.returnDate.set(returnDate);
        if (returnDate != null && dueDate.get() != null) {
            setFineAmount(DateUtils.calculateFine(dueDate.get(), returnDate));
        }
    }
    public void setFineAmount(double fineAmount) { this.fineAmount.set(fineAmount); }
    public void setIsReturned(boolean isReturned) { this.isReturned.set(isReturned); }
    public void setBookTitle(String bookTitle) { this.bookTitle.set(bookTitle); }
    public void setUserFullName(String userFullName) { this.userFullName.set(userFullName); }
    
    // Utility methods
    public boolean isOverdue() {
        return !isReturned.get() && DateUtils.isOverdue(dueDate.get());
    }
    
    public boolean isDueSoon() {
        return !isReturned.get() && DateUtils.isDueSoon(dueDate.get());
    }
    
    public String getStatus() {
        if (isReturned.get()) {
            return "Returned";
        } else if (isOverdue()) {
            return "Overdue";
        } else if (isDueSoon()) {
            return "Due Soon";
        } else {
            return "Active";
        }
    }
    
    @Override
    public String toString() {
        return "Issue #" + issueId.get() + " - " + bookTitle.get() + " to " + userFullName.get();
    }
}