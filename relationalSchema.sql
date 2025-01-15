DROP TABLE IF EXISTS written_by,
                     borrowed_by,
                     author_pnum,
                     published_by,
                     publisher_pnum,
                     located_at,
                     book,
                     author,
                     member,
                     publisher,
                     library,
                     phone,
                     audit_log;

CREATE TABLE book(
    isbn        CHAR(14)        NOT NULL,
    title       VARCHAR(255)    NOT NULL,
    pub_year    DECIMAL(4,0)    NOT NULL,
    PRIMARY KEY (isbn)
);

CREATE TABLE author(
    authorid    INT             NOT NULL,    
    first_name  VARCHAR(255)    NOT NULL,
    last_name   VARCHAR(255)    NOT NULL,
    PRIMARY KEY (authorid)
);

CREATE TABLE member(
    memberid    INT             NOT NULL,
    first_name  VARCHAR(255)    NOT NULL,
    last_name   VARCHAR(255)    NOT NULL,
    birth_date  DATE            NOT NULL,
    gender      ENUM ('M', 'F') NOT NULL,
    PRIMARY KEY (memberid)
);

CREATE TABLE publisher(
    pubid       INT             NOT NULL,
    pub_name    VARCHAR(255)    NOT NULL,
    PRIMARY KEY (pubid)
);

CREATE TABLE phone(
    pnumber CHAR(12)                NOT NULL,
    ptype   ENUM ('c', 'h', 'o')    NOT NULL,
    PRIMARY KEY (pnumber)
);

CREATE TABLE library(
    library_name    VARCHAR(255)    NOT NULL,
    address_street  VARCHAR(255)    NOT NULL,
    address_city    VARCHAR(255)    NOT NULL,
    address_state   CHAR(2)         NOT NULL,
    PRIMARY KEY (library_name)
);

CREATE TABLE written_by(
    isbn        CHAR(14)    NOT NULL,
    authorid    INT         NOT NULL,
    FOREIGN KEY (isbn)      REFERENCES book (isbn)          ON DELETE CASCADE,
    FOREIGN KEY (authorid)  REFERENCES author (authorid)    ON DELETE CASCADE,
    PRIMARY KEY (isbn, authorid)
);

CREATE TABLE borrowed_by(
    isbn            CHAR(14)        NOT NULL,
    memberid        INT             NOT NULL,
    library_name    VARCHAR(255)    NOT NULL,
    out_date        DATE            NOT NULL,
    in_date         DATE,
    FOREIGN KEY (isbn)          REFERENCES book (isbn)              ON DELETE CASCADE,
    FOREIGN KEY (memberid)      REFERENCES member (memberid)        ON DELETE CASCADE,
    FOREIGN KEY (library_name)  REFERENCES library (library_name)   ON DELETE CASCADE,
    PRIMARY KEY (isbn, memberid, library_name, out_date)
);

CREATE TABLE author_pnum(
    authorid    INT         NOT NULL,
    pnumber     CHAR(12)    NOT NULL,
    FOREIGN KEY (authorid)  REFERENCES author (authorid)  ON DELETE CASCADE,
    FOREIGN KEY (pnumber)   REFERENCES phone (pnumber)    ON DELETE CASCADE,
    PRIMARY KEY (authorid, pnumber)
);

CREATE TABLE published_by(
    isbn        CHAR(14)    NOT NULL,
    pubid       INT         NOT NULL,
    FOREIGN KEY (isbn)  REFERENCES book (isbn)          ON DELETE CASCADE,
    FOREIGN KEY (pubid) REFERENCES publisher (pubid)    ON DELETE CASCADE,
    PRIMARY KEY (isbn)
);

CREATE TABLE publisher_pnum(
    pubid       INT         NOT NULL,
    pnumber     CHAR(12)    NOT NULL,
    FOREIGN KEY (pubid)     REFERENCES publisher (pubid)  ON DELETE CASCADE,
    FOREIGN KEY (pnumber)   REFERENCES phone (pnumber)    ON DELETE CASCADE,
    PRIMARY KEY (pubid, pnumber)
);

CREATE TABLE located_at(
    library_name        VARCHAR(255)    NOT NULL,
    isbn                CHAR(14)        NOT NULL,
    num_copies          INT             NOT NULL,
    available_copies    INT             NOT NULL,
    shelf               INT             NOT NULL,
    floor               INT             NOT NULL,
    FOREIGN KEY (library_name)  REFERENCES library (library_name)   ON DELETE CASCADE,
    FOREIGN KEY (isbn)          REFERENCES book (isbn)              ON DELETE CASCADE,
    PRIMARY KEY (library_name, isbn)
);

CREATE TABLE audit_log(
    audit_id    INT AUTO_INCREMENT PRIMARY KEY,
    table_name  VARCHAR(255),
    action_type CHAR (6) NOT NULL,
    action_date DATE NOT NULL,
    action_time TIME NOT NULL
) AUTO_INCREMENT = 1000;

DELIMITER //
CREATE TRIGGER book_checkout
BEFORE INSERT ON borrowed_by
FOR EACH ROW
BEGIN
    DECLARE avail_copies INT;
    SELECT l.available_copies INTO avail_copies
    FROM located_at l 
    WHERE NEW.library_name = l.library_name AND NEW.isbn = l.isbn;

    IF NEW.in_date IS NULL THEN
        IF avail_copies > 0 THEN
            UPDATE located_at l 
            SET available_copies = available_copies - 1 
            WHERE NEW.library_name = l.library_name AND NEW.isbn = l.isbn;
        ELSE 
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = "No copies available";
        END IF;
    END IF;
END //
DELIMITER ;


DELIMITER //
CREATE TRIGGER book_checkin
AFTER UPDATE ON borrowed_by
FOR EACH ROW 
BEGIN 
    IF NEW.in_date IS NOT NULL AND OLD.in_date is NULL THEN
        UPDATE located_at l
        SET available_copies = available_copies + 1
        WHERE NEW.library_name = l.library_name AND NEW.isbn = l.isbn;
    END IF;
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER update_avail_book_quantity
BEFORE UPDATE ON located_at
FOR EACH ROW
BEGIN 
    IF NEW.num_copies != OLD.num_copies THEN 
        SET NEW.available_copies = OLD.available_copies + (NEW.num_copies - OLD.num_copies);
    END IF;
END //
DELIMITER ;