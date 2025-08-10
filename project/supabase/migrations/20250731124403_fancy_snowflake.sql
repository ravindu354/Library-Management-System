-- Library Management System Database Schema
-- Complete database structure for managing books, users, and transactions

-- Create database (run this separately if needed)
-- CREATE DATABASE IF NOT EXISTS library_management_system;
-- USE library_management_system;

-- Drop existing tables if they exist (for fresh setup)
DROP TABLE IF EXISTS book_issues;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS users;

-- Users table - stores all system users (Students, Faculty, Librarians)
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    phone_number VARCHAR(20),
    role ENUM('Student', 'Faculty', 'Librarian') NOT NULL DEFAULT 'Student',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes for better performance
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_is_active (is_active)
);

-- Books table - stores all library books
CREATE TABLE books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    total_copies INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_total_copies CHECK (total_copies >= 0),
    CONSTRAINT chk_available_copies CHECK (available_copies >= 0),
    CONSTRAINT chk_available_le_total CHECK (available_copies <= total_copies),
    
    -- Indexes for better performance
    INDEX idx_title (title),
    INDEX idx_author (author),
    INDEX idx_category (category),
    INDEX idx_isbn (isbn),
    INDEX idx_is_active (is_active),
    INDEX idx_available_copies (available_copies)
);

-- Book Issues table - tracks book borrowing and returns
CREATE TABLE book_issues (
    issue_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE NULL,
    fine_amount DECIMAL(10, 2) DEFAULT 0.00,
    is_returned BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    
    -- Constraints
    CONSTRAINT chk_due_date CHECK (due_date >= issue_date),
    CONSTRAINT chk_return_date CHECK (return_date IS NULL OR return_date >= issue_date),
    CONSTRAINT chk_fine_amount CHECK (fine_amount >= 0),
    
    -- Indexes for better performance
    INDEX idx_user_id (user_id),
    INDEX idx_book_id (book_id),
    INDEX idx_issue_date (issue_date),
    INDEX idx_due_date (due_date),
    INDEX idx_return_date (return_date),
    INDEX idx_is_returned (is_returned),
    INDEX idx_overdue (due_date, is_returned)
);

-- Create a view for active issues (not returned)
CREATE VIEW active_issues AS
SELECT 
    bi.issue_id,
    bi.user_id,
    bi.book_id,
    bi.issue_date,
    bi.due_date,
    bi.fine_amount,
    b.title as book_title,
    b.author as book_author,
    CONCAT(u.first_name, ' ', u.last_name) as user_name,
    u.role as user_role,
    CASE 
        WHEN bi.due_date < CURDATE() THEN 'Overdue'
        WHEN DATEDIFF(bi.due_date, CURDATE()) <= 2 THEN 'Due Soon'
        ELSE 'Active'
    END as status
FROM book_issues bi
JOIN books b ON bi.book_id = b.book_id
JOIN users u ON bi.user_id = u.user_id
WHERE bi.is_returned = FALSE;

-- Create a view for overdue books
CREATE VIEW overdue_books AS
SELECT 
    bi.issue_id,
    bi.user_id,
    bi.book_id,
    bi.issue_date,
    bi.due_date,
    DATEDIFF(CURDATE(), bi.due_date) as days_overdue,
    DATEDIFF(CURDATE(), bi.due_date) * 1.0 as current_fine,
    b.title as book_title,
    b.author as book_author,
    CONCAT(u.first_name, ' ', u.last_name) as user_name,
    u.email as user_email,
    u.phone_number as user_phone
FROM book_issues bi
JOIN books b ON bi.book_id = b.book_id
JOIN users u ON bi.user_id = u.user_id
WHERE bi.is_returned = FALSE 
    AND bi.due_date < CURDATE();

-- Triggers for maintaining data integrity

-- Trigger to update book availability when book is issued
DELIMITER //
CREATE TRIGGER tr_book_issued
    AFTER INSERT ON book_issues
    FOR EACH ROW
BEGIN
    IF NEW.is_returned = FALSE THEN
        UPDATE books 
        SET available_copies = available_copies - 1 
        WHERE book_id = NEW.book_id;
    END IF;
END//

-- Trigger to update book availability when book is returned
CREATE TRIGGER tr_book_returned
    AFTER UPDATE ON book_issues
    FOR EACH ROW
BEGIN
    IF OLD.is_returned = FALSE AND NEW.is_returned = TRUE THEN
        UPDATE books 
        SET available_copies = available_copies + 1 
        WHERE book_id = NEW.book_id;
    END IF;
END//

-- Trigger to prevent issuing books when no copies available
CREATE TRIGGER tr_check_availability
    BEFORE INSERT ON book_issues
    FOR EACH ROW
BEGIN
    DECLARE available_count INT;
    
    SELECT available_copies INTO available_count
    FROM books 
    WHERE book_id = NEW.book_id AND is_active = TRUE;
    
    IF available_count <= 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'No copies of this book are available for issue';
    END IF;
END//

DELIMITER ;

-- Create stored procedures for common operations

-- Procedure to issue a book
DELIMITER //
CREATE PROCEDURE sp_issue_book(
    IN p_user_id INT,
    IN p_book_id INT,
    IN p_issue_date DATE,
    IN p_due_date DATE
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    -- Insert issue record
    INSERT INTO book_issues (user_id, book_id, issue_date, due_date)
    VALUES (p_user_id, p_book_id, p_issue_date, p_due_date);
    
    COMMIT;
END//

-- Procedure to return a book
CREATE PROCEDURE sp_return_book(
    IN p_issue_id INT,
    IN p_return_date DATE,
    IN p_fine_amount DECIMAL(10,2)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    START TRANSACTION;
    
    -- Update issue record
    UPDATE book_issues 
    SET return_date = p_return_date,
        fine_amount = p_fine_amount,
        is_returned = TRUE
    WHERE issue_id = p_issue_id;
    
    COMMIT;
END//

-- Procedure to get user statistics
CREATE PROCEDURE sp_get_user_stats(IN p_user_id INT)
BEGIN
    SELECT 
        COUNT(*) as total_issues,
        COUNT(CASE WHEN is_returned = FALSE THEN 1 END) as active_issues,
        COUNT(CASE WHEN is_returned = FALSE AND due_date < CURDATE() THEN 1 END) as overdue_issues,
        COALESCE(SUM(fine_amount), 0) as total_fines_paid
    FROM book_issues 
    WHERE user_id = p_user_id;
END//

-- Procedure to get library statistics
CREATE PROCEDURE sp_get_library_stats()
BEGIN
    SELECT 
        (SELECT COUNT(*) FROM books WHERE is_active = TRUE) as total_books,
        (SELECT SUM(available_copies) FROM books WHERE is_active = TRUE) as available_copies,
        (SELECT COUNT(*) FROM users WHERE is_active = TRUE) as total_users,
        (SELECT COUNT(*) FROM book_issues WHERE is_returned = FALSE) as active_issues,
        (SELECT COUNT(*) FROM book_issues WHERE is_returned = FALSE AND due_date < CURDATE()) as overdue_issues;
END//

DELIMITER ;

-- Future enhancement placeholders (commented out for now)

/*
-- Table for digital resources (future enhancement)
CREATE TABLE digital_resources (
    resource_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255),
    resource_type ENUM('eBook', 'Audio', 'Video', 'Article') NOT NULL,
    file_path VARCHAR(500),
    access_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table for reservation system (future enhancement)  
CREATE TABLE book_reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    reservation_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status ENUM('Active', 'Fulfilled', 'Expired', 'Cancelled') DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (book_id) REFERENCES books(book_id)
);

-- Table for library events/announcements (future enhancement)
CREATE TABLE library_announcements (
    announcement_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    target_role ENUM('All', 'Student', 'Faculty', 'Librarian') DEFAULT 'All',
    is_active BOOLEAN DEFAULT TRUE,
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);
*/

-- Add comments to tables for documentation
ALTER TABLE users COMMENT = 'Stores all system users including students, faculty, and librarians';
ALTER TABLE books COMMENT = 'Contains all library books with availability tracking';
ALTER TABLE book_issues COMMENT = 'Tracks all book borrowing transactions and returns';

-- Grant appropriate permissions (adjust based on your setup)
-- GRANT SELECT, INSERT, UPDATE, DELETE ON library_management_system.* TO 'library_user'@'localhost';

-- Database setup complete
SELECT 'Library Management System database schema created successfully!' as Status;