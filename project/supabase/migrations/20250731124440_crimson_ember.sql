-- Library Management System - Sample Data
-- Insert test data for demonstration and development purposes

-- Clear existing data (in proper order to respect foreign keys)
DELETE FROM book_issues;
DELETE FROM books;
DELETE FROM users;

-- Reset auto-increment counters
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE books AUTO_INCREMENT = 1;
ALTER TABLE book_issues AUTO_INCREMENT = 1;

-- Insert sample users
INSERT INTO users (username, password, first_name, last_name, email, phone_number, role, is_active) VALUES
-- Librarians
('admin', 'admin123', 'Sarah', 'Johnson', 'sarah.johnson@library.edu', '555-0101', 'Librarian', TRUE),
('lib_mary', 'lib123', 'Mary', 'Smith', 'mary.smith@library.edu', '555-0102', 'Librarian', TRUE),

-- Faculty Members
('faculty1', 'faculty123', 'Dr. Robert', 'Brown', 'robert.brown@university.edu', '555-0201', 'Faculty', TRUE),
('prof_wilson', 'prof123', 'Prof. Emily', 'Wilson', 'emily.wilson@university.edu', '555-0202', 'Faculty', TRUE),
('dr_davis', 'davis123', 'Dr. Michael', 'Davis', 'michael.davis@university.edu', '555-0203', 'Faculty', TRUE),
('prof_garcia', 'garcia123', 'Prof. Maria', 'Garcia', 'maria.garcia@university.edu', '555-0204', 'Faculty', TRUE),

-- Students
('student1', 'student123', 'John', 'Doe', 'john.doe@student.edu', '555-0301', 'Student', TRUE),
('alice_walker', 'alice123', 'Alice', 'Walker', 'alice.walker@student.edu', '555-0302', 'Student', TRUE),
('bob_miller', 'bob123', 'Bob', 'Miller', 'bob.miller@student.edu', '555-0303', 'Student', TRUE),
('charlie_jones', 'charlie123', 'Charlie', 'Jones', 'charlie.jones@student.edu', '555-0304', 'Student', TRUE),
('diana_taylor', 'diana123', 'Diana', 'Taylor', 'diana.taylor@student.edu', '555-0305', 'Student', TRUE),
('eve_anderson', 'eve123', 'Eve', 'Anderson', 'eve.anderson@student.edu', '555-0306', 'Student', TRUE),
('frank_thomas', 'frank123', 'Frank', 'Thomas', 'frank.thomas@student.edu', '555-0307', 'Student', TRUE),
('grace_white', 'grace123', 'Grace', 'White', 'grace.white@student.edu', '555-0308', 'Student', TRUE);

-- Insert sample books across various categories
INSERT INTO books (title, author, category, isbn, total_copies, available_copies, is_active) VALUES
-- Computer Science
('Introduction to Algorithms', 'Thomas H. Cormen', 'Computer Science', '978-0262033848', 5, 3, TRUE),
('Design Patterns', 'Gang of Four', 'Computer Science', '978-0201633612', 3, 2, TRUE),
('Clean Code', 'Robert C. Martin', 'Computer Science', '978-0132350884', 4, 4, TRUE),
('Database System Concepts', 'Abraham Silberschatz', 'Computer Science', '978-0073523323', 3, 1, TRUE),
('Computer Networks', 'Andrew S. Tanenbaum', 'Computer Science', '978-0132126953', 2, 2, TRUE),

-- Mathematics
('Calculus: Early Transcendentals', 'James Stewart', 'Mathematics', '978-1285741550', 6, 4, TRUE),
('Linear Algebra and Its Applications', 'David C. Lay', 'Mathematics', '978-0321982384', 4, 3, TRUE),
('Discrete Mathematics', 'Kenneth H. Rosen', 'Mathematics', '978-0073383095', 3, 2, TRUE),
('Statistics for Engineers', 'Douglas C. Montgomery', 'Mathematics', '978-0470631478', 2, 2, TRUE),

-- Physics
('Principles of Physics', 'David Halliday', 'Physics', '978-1118230718', 5, 3, TRUE),
('University Physics', 'Hugh D. Young', 'Physics', '978-0321973610', 4, 2, TRUE),
('Modern Physics', 'Randy Harris', 'Physics', '978-0805303087', 2, 1, TRUE),

-- Literature
('To Kill a Mockingbird', 'Harper Lee', 'Literature', '978-0061120084', 8, 6, TRUE),
('1984', 'George Orwell', 'Literature', '978-0452284234', 6, 4, TRUE),
('Pride and Prejudice', 'Jane Austen', 'Literature', '978-0141439518', 5, 3, TRUE),
('The Great Gatsby', 'F. Scott Fitzgerald', 'Literature', '978-0743273565', 4, 2, TRUE),
('One Hundred Years of Solitude', 'Gabriel García Márquez', 'Literature', '978-0060883287', 3, 3, TRUE),

-- History
('A People\'s History of the United States', 'Howard Zinn', 'History', '978-0062397348', 4, 2, TRUE),
('The Guns of August', 'Barbara Tuchman', 'History', '978-0345476098', 3, 3, TRUE),
('Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', 'History', '978-0062316097', 5, 1, TRUE),

-- Economics
('Principles of Economics', 'N. Gregory Mankiw', 'Economics', '978-1305585126', 4, 3, TRUE),
('Freakonomics', 'Steven D. Levitt', 'Economics', '978-0060731335', 3, 2, TRUE),

-- Psychology
('Psychology: The Science of Mind and Behaviour', 'Michael W. Passer', 'Psychology', '978-0077172824', 3, 2, TRUE),
('Thinking, Fast and Slow', 'Daniel Kahneman', 'Psychology', '978-0374533557', 4, 3, TRUE),

-- Philosophy
('The Republic', 'Plato', 'Philosophy', '978-0380016075', 3, 2, TRUE),
('Meditations', 'Marcus Aurelius', 'Philosophy', '978-0486298238', 2, 2, TRUE),

-- Biology
('Campbell Biology', 'Jane B. Reece', 'Biology', '978-0134093413', 5, 3, TRUE),
('Molecular Biology of the Cell', 'Bruce Alberts', 'Biology', '978-0815344322', 3, 1, TRUE);

-- Insert sample book issues (some active, some returned, some overdue)
INSERT INTO book_issues (user_id, book_id, issue_date, due_date, return_date, fine_amount, is_returned) VALUES
-- Returned books
(7, 1, '2024-01-15', '2024-01-29', '2024-01-28', 0.00, TRUE),
(8, 2, '2024-01-20', '2024-02-03', '2024-02-05', 2.00, TRUE),
(9, 3, '2024-02-01', '2024-02-15', '2024-02-14', 0.00, TRUE),
(10, 13, '2024-02-10', '2024-02-24', '2024-02-22', 0.00, TRUE),
(11, 14, '2024-02-15', '2024-03-01', '2024-03-03', 2.00, TRUE),

-- Currently active issues (not overdue)
(7, 5, '2024-11-15', '2024-11-29', NULL, 0.00, FALSE),
(8, 6, '2024-11-18', '2024-12-02', NULL, 0.00, FALSE),
(12, 15, '2024-11-20', '2024-12-04', NULL, 0.00, FALSE),
(13, 16, '2024-11-22', '2024-12-06', NULL, 0.00, FALSE),

-- Overdue books (should generate fines)
(9, 4, '2024-11-01', '2024-11-15', NULL, 0.00, FALSE),
(10, 20, '2024-11-05', '2024-11-19', NULL, 0.00, FALSE),
(14, 25, '2024-10-28', '2024-11-11', NULL, 0.00, FALSE),

-- Books due soon (within 2 days)
(11, 11, CURDATE() - INTERVAL 12 DAY, CURDATE() + INTERVAL 1 DAY, NULL, 0.00, FALSE),
(12, 12, CURDATE() - INTERVAL 11 DAY, CURDATE() + INTERVAL 2 DAY, NULL, 0.00, FALSE),

-- Faculty member active issues
(3, 1, '2024-11-10', '2024-11-24', NULL, 0.00, FALSE),
(4, 2, '2024-11-12', '2024-11-26', NULL, 0.00, FALSE),
(5, 18, '2024-11-14', '2024-11-28', NULL, 0.00, FALSE);

-- Update book availability based on active issues
UPDATE books SET available_copies = total_copies - (
    SELECT COUNT(*) FROM book_issues 
    WHERE book_issues.book_id = books.book_id AND is_returned = FALSE
);

-- Create some sample data for testing search functionality
-- Add a few more books with specific patterns for search testing
INSERT INTO books (title, author, category, isbn, total_copies, available_copies, is_active) VALUES
('Advanced Java Programming', 'John Smith', 'Computer Science', '978-1234567890', 2, 2, TRUE),
('Java: The Complete Reference', 'Herbert Schildt', 'Computer Science', '978-1234567891', 3, 3, TRUE),
('Python Machine Learning', 'Sebastian Raschka', 'Computer Science', '978-1234567892', 2, 2, TRUE),
('Data Structures in C++', 'Michael Goodrich', 'Computer Science', '978-1234567893', 2, 2, TRUE);

-- Insert a few test issues for report generation
INSERT INTO book_issues (user_id, book_id, issue_date, due_date, return_date, fine_amount, is_returned) VALUES
-- More historical data for reports
(7, 27, '2024-09-01', '2024-09-15', '2024-09-20', 5.00, TRUE),
(8, 28, '2024-09-05', '2024-09-19', '2024-09-18', 0.00, TRUE),
(9, 29, '2024-09-10', '2024-09-24', '2024-09-30', 6.00, TRUE),
(10, 30, '2024-10-01', '2024-10-15', '2024-10-16', 1.00, TRUE),
(11, 27, '2024-10-05', '2024-10-19', '2024-10-20', 1.00, TRUE);

-- Final update to ensure book availability is correct
UPDATE books SET available_copies = total_copies - (
    SELECT COUNT(*) FROM book_issues 
    WHERE book_issues.book_id = books.book_id AND is_returned = FALSE
);

-- Display summary of inserted data
SELECT 'Data insertion completed successfully!' as Status;

SELECT 
    (SELECT COUNT(*) FROM users) as Total_Users,
    (SELECT COUNT(*) FROM users WHERE role = 'Student') as Students,
    (SELECT COUNT(*) FROM users WHERE role = 'Faculty') as Faculty,
    (SELECT COUNT(*) FROM users WHERE role = 'Librarian') as Librarians;

SELECT 
    (SELECT COUNT(*) FROM books) as Total_Books,
    (SELECT SUM(total_copies) FROM books) as Total_Copies,
    (SELECT SUM(available_copies) FROM books) as Available_Copies;

SELECT 
    (SELECT COUNT(*) FROM book_issues) as Total_Issues,
    (SELECT COUNT(*) FROM book_issues WHERE is_returned = TRUE) as Returned_Issues,
    (SELECT COUNT(*) FROM book_issues WHERE is_returned = FALSE) as Active_Issues,
    (SELECT COUNT(*) FROM book_issues WHERE is_returned = FALSE AND due_date < CURDATE()) as Overdue_Issues;

-- Show some sample login credentials for testing
SELECT 'Sample Login Credentials:' as Info;
SELECT username, role, CONCAT(first_name, ' ', last_name) as full_name 
FROM users 
WHERE username IN ('admin', 'faculty1', 'student1')
ORDER BY role;