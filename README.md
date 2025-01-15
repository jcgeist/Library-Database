# Relational Database Library System
Mock Library Database using MySql and Java

## Project Overview
This project is a relational database system for a library, implemented with a Java GUI and SQL backend. It allows users to interact with the library database by:
- Searching for books by ISBN, title, or author.
- Adding new members to the library system.
- Validating existing members.

The application integrates both the user interface and backend logic into a single Java file for simplicity and submission purposes. 

**Note:** The original database server is no longer operational. Users must set up their own MySQL server and update the connection details in the code to use this application.

## Features
### Java GUI
- **Member Login**: Validates member IDs to access the system.
- **Book Search**: Allows users to search books using multiple criteria (ISBN, author, title).
- **Member Registration**: Enables new members to register by providing their details.
- **Dynamic Tables**: Displays search results in a table format.
- **Error Handling**: Informs users about invalid inputs, such as incorrect IDs or dates.

### SQL Backend
- **Database Schema**: Contains tables for members, books, authors, and book locations.
- **Queries**: Includes SQL queries for efficient data retrieval and management.
- **Triggers and Views**: Utilizes SQL triggers and views to enhance database functionality.

## Components
1. **Java Application (`Lab10Geisterfer.java`)**:
   - Implements the GUI and backend logic.
   - Interacts with the database using JDBC.
   - Includes error-handling mechanisms for user input validation.

2. **SQL Files**:
   - **`relationalSchema.sql`**: Defines the relational schema for the database.
   - **`queryTriggerView.sql`**: Includes SQL queries, triggers, and views.
   - **`activity.sql`**: Populates the database with sample data.
   - **`LoadLib.sql`**: Prepares the database with initial configurations.

3. **Python Utility (`createDataDump.py`)**:
   - Used for creating backups or managing database dumps.

## Installation and Setup
### Prerequisites
- **Java Development Kit (JDK)**: Version 11 or higher.
- **MySQL Server**: Required to set up your own database server.
- **Python**: Required for `createDataDump.py` (optional).

### Steps
1. **Set Up MySQL Server**:
   - Install MySQL Server on your system.
   - Create a new database.

2. **Import the Database Schema**:
   - Open `relationalSchema.sql` in a MySQL client and execute the script to set up the schema.

3. **Load Initial Data**:
   - Run `activity.sql` and `LoadLib.sql` to populate the database with sample data.

4. **Update Database Connection Details**:
   - In the `SQLRelay` class of `Lab10Geisterfer.java`, update the following variables with your database connection details:
     ```java
     static final String DB_URL = "jdbc:mysql://<your-database-url>:3306/<your-database-name>";
     static final String USER = "<your-username>";
     static final String PASS = "<your-password>";
     ```

5. **Compile and Run the Java Application**:
   ```bash
   javac Lab10Geisterfer.java
   java Lab10Geisterfer
