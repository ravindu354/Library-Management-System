package com.library.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date operations
 * Handles date calculations for library system
 */
public class DateUtils {
    
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATABASE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Calculate due date from issue date
     */
    public static LocalDate calculateDueDate(LocalDate issueDate) {
        return issueDate.plusDays(DatabaseConstants.LOAN_PERIOD_DAYS);
    }
    
    /**
     * Calculate fine for overdue books
     */
    public static double calculateFine(LocalDate dueDate, LocalDate returnDate) {
        if (returnDate.isAfter(dueDate)) {
            long overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);
            return overdueDays * DatabaseConstants.FINE_PER_DAY;
        }
        return 0.0;
    }
    
    /**
     * Check if book is overdue
     */
    public static boolean isOverdue(LocalDate dueDate) {
        return LocalDate.now().isAfter(dueDate);
    }
    
    /**
     * Check if book is due soon (within warning period)
     */
    public static boolean isDueSoon(LocalDate dueDate) {
        long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
        return daysUntilDue <= DatabaseConstants.WARNING_DAYS && daysUntilDue >= 0;
    }
    
    /**
     * Format date for display
     */
    public static String formatForDisplay(LocalDate date) {
        return date != null ? date.format(DISPLAY_FORMAT) : "";
    }
    
    /**
     * Format date for database storage
     */
    public static String formatForDatabase(LocalDate date) {
        return date != null ? date.format(DATABASE_FORMAT) : null;
    }
    
    /**
     * Parse date from string
     */
    public static LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString, DISPLAY_FORMAT);
        } catch (Exception e) {
            try {
                return LocalDate.parse(dateString, DATABASE_FORMAT);
            } catch (Exception ex) {
                return null;
            }
        }
    }
    
    /**
     * Get days difference between two dates
     */
    public static long getDaysBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }
}