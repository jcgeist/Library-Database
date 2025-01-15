-- List the contents of the Library relation in order according to name.
SELECT * 
FROM library
ORDER BY library_name;

-- List the contents of the Located at relation in alphabetic order according to ISBN.
SELECT *
FROM located_at
ORDER BY library_name, isbn;

-- For each book that has copies in both libraries, list the book name, number of copies, and library sorted by book name.
SELECT b.title, l.num_copies, l.library_name
FROM book b, located_at l, located_at ll
WHERE l.isbn = ll.isbn and l.library_name != ll.library_name and b.isbn = l.isbn
ORDER BY b.title;

--For each library, list library name, and the number of titles sorted by library.
SELECT library_name, count(isbn) as num_titles
FROM located_at
GROUP BY library_name
ORDER BY library_name;

-- Audit Triggers
CREATE TRIGGER author_audit
AFTER INSERT ON author
FOR EACH ROW
INSERT INTO audit_log(table_name, action_type, action_date, action_time) VALUES ('author','insert',CURDATE(),CURTIME());

CREATE TRIGGER add_book_audit
AFTER INSERT ON located_at
FOR EACH ROW
INSERT INTO audit_log(table_name, action_type, action_date, action_time) VALUES ('located_at','insert',CURDATE(),CURTIME());

CREATE TRIGGER delete_book_audit
AFTER DELETE ON located_at
FOR EACH ROW
INSERT INTO audit_log(table_name, action_type, action_date, action_time) VALUES ('located_at','delete',CURDATE(),CURTIME());

DELIMITER //
CREATE TRIGGER update_book_quantity
AFTER UPDATE ON located_at
FOR EACH ROW
BEGIN 
    IF NEW.available_copies != OLD.available_copies OR NEW.num_copies != OLD.num_copies THEN
        INSERT INTO audit_log(table_name, action_type, action_date, action_time) VALUES ('located_at','update',CURDATE(),CURTIME());
    END IF;
END //
DELIMITER ;

-- View 
CREATE OR REPLACE SQL SECURITY INVOKER VIEW book_author_library AS
    SELECT b.title, GROUP_CONCAT(CONCAT_WS(' ',a.first_name, a.last_name) SEPARATOR ', ') as authors, l.library_name, b.isbn
    FROM book b, written_by w, author a, located_at l
    WHERE b.isbn = w.isbn and w.authorid = a.authorid and l.isbn = b.isbn
    GROUP BY l.library_name, b.title;

-- Using this view, provide a list of books, authors, shelf, and library name sorted by book name.
SELECT title, authors, shelf, l.library_name
FROM book_author_library b NATURAL JOIN located_at l
ORDER BY title;