package com.library.models;

/**
 * Enumeration for user roles in the library system
 */
public enum UserRole {
    STUDENT("Student"),
    FACULTY("Faculty"),
    LIBRARIAN("Librarian");
    
    private final String displayName;
    
    UserRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    public static UserRole fromString(String text) {
        for (UserRole role : UserRole.values()) {
            if (role.displayName.equalsIgnoreCase(text)) {
                return role;
            }
        }
        return STUDENT; // Default fallback
    }
}