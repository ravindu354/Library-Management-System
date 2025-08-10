package com.library.models;

import javafx.beans.property.*;

/**
 * User model class representing library users
 * Supports Students, Faculty, and Librarians
 */
public class User {
    
    private final IntegerProperty userId;
    private final StringProperty username;
    private final StringProperty password;
    private final StringProperty firstName;
    private final StringProperty lastName;
    private final StringProperty email;
    private final StringProperty phoneNumber;
    private final ObjectProperty<UserRole> role;
    private final BooleanProperty isActive;
    
    // Default constructor
    public User() {
        this.userId = new SimpleIntegerProperty();
        this.username = new SimpleStringProperty("");
        this.password = new SimpleStringProperty("");
        this.firstName = new SimpleStringProperty("");
        this.lastName = new SimpleStringProperty("");
        this.email = new SimpleStringProperty("");
        this.phoneNumber = new SimpleStringProperty("");
        this.role = new SimpleObjectProperty<>(UserRole.STUDENT);
        this.isActive = new SimpleBooleanProperty(true);
    }
    
    // Constructor with parameters
    public User(int userId, String username, String password, String firstName, 
                String lastName, String email, String phoneNumber, UserRole role, boolean isActive) {
        this();
        setUserId(userId);
        setUsername(username);
        setPassword(password);
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setRole(role);
        setIsActive(isActive);
    }
    
    // Property getters
    public IntegerProperty userIdProperty() { return userId; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty passwordProperty() { return password; }
    public StringProperty firstNameProperty() { return firstName; }
    public StringProperty lastNameProperty() { return lastName; }
    public StringProperty emailProperty() { return email; }
    public StringProperty phoneNumberProperty() { return phoneNumber; }
    public ObjectProperty<UserRole> roleProperty() { return role; }
    public BooleanProperty isActiveProperty() { return isActive; }
    
    // Getters
    public int getUserId() { return userId.get(); }
    public String getUsername() { return username.get(); }
    public String getPassword() { return password.get(); }
    public String getFirstName() { return firstName.get(); }
    public String getLastName() { return lastName.get(); }
    public String getEmail() { return email.get(); }
    public String getPhoneNumber() { return phoneNumber.get(); }
    public UserRole getRole() { return role.get(); }
    public boolean getIsActive() { return isActive.get(); }
    
    // Setters
    public void setUserId(int userId) { this.userId.set(userId); }
    public void setUsername(String username) { this.username.set(username); }
    public void setPassword(String password) { this.password.set(password); }
    public void setFirstName(String firstName) { this.firstName.set(firstName); }
    public void setLastName(String lastName) { this.lastName.set(lastName); }
    public void setEmail(String email) { this.email.set(email); }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber.set(phoneNumber); }
    public void setRole(UserRole role) { this.role.set(role); }
    public void setIsActive(boolean isActive) { this.isActive.set(isActive); }
    
    // Utility methods
    public String getFullName() {
        return firstName.get() + " " + lastName.get();
    }
    
    @Override
    public String toString() {
        return getFullName() + " (" + getRole().getDisplayName() + ")";
    }
}