import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lab10Geisterfer {

    public class SQLRelay{
        public class MemberNotFoundException extends Exception{
            public MemberNotFoundException(String msg){
                super(msg);
            }
        }

        static final String DB_URL = "jdbc:mysql://faure.cs.colostate.edu:3306/jcgeist";
        static final String USER = "jcgeist";
        static final String PASS = "820604562";

        private Connection makeConnection(){
            Connection conn = null;
            try{
                conn = DriverManager.getConnection(DB_URL, USER, PASS);
            }catch(Exception e){
                System.out.println("Failed to connect to server");
            }
            return conn;
        }
        private void closeConnection(Connection conn){
            try{
                conn.close();
            }catch(Exception e){
                System.out.println("Failed to close connection");
            }
        }
        public void verifyMember(int memberid) throws MemberNotFoundException, SQLException{
            Connection conn = makeConnection();
            String sql = createSQLString("*", "member", String.format("memberid = %d", memberid));
            ResultSet rs = getResults(conn, sql);
            if(!rs.next()){throw new MemberNotFoundException("Member not in system");}
            closeConnection(conn);
        }
        private String createSQLString(String select, String from, String where){
            String sql = String.format("SELECT %s FROM %s WHERE %s", select, from, where);
            return sql;
        }
        private ResultSet getResults(Connection conn, String sqlStatement) throws SQLException{
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sqlStatement);
            return rs;
        }

        public String[][] searchBooks(String isbn, String author, String title){
            String searchString = createBookSearchString(isbn, author, title);
            Connection conn = makeConnection();
            String[][] formatedData = null;
            try{
                ResultSet rs = getResults(conn, searchString);
                formatedData = formatResults(rs);
            }catch(SQLException e){
                System.out.println(e.getMessage());
            }
            closeConnection(conn);
            return formatedData;
        }
        private String withAs(){
            String with = 
                "WITH bookSearchView AS "+
                "(SELECT b.title, GROUP_CONCAT(CONCAT_WS(' ',a.first_name, a.last_name) SEPARATOR ', ') as authors, l.library_name, b.isbn, l.shelf, l.available_copies " +
                "FROM book b, written_by w, author a, located_at l " +
                "WHERE b.isbn = w.isbn and w.authorid = a.authorid and l.isbn = b.isbn " + 
                "GROUP BY l.library_name, b.title) ";
            return with;
        }
        private String createBookSearchString(String isbn, String author, String title){
            String with = withAs();
            String where = createBookWhere(isbn, author, title);
            return with + createSQLString("*", "bookSearchView", where);
        }
        private String createBookWhere(String isbn, String author, String title){
            String where = "1=1";
            if(isbn != null){
                where += String.format(" AND isbn = '%s'", isbn);
            }
            if(author != null){
                where += String.format(" AND authors LIKE '%%%s%%'", author);
            }
            if(title != null){
                where += String.format(" AND title LIKE '%%%s%%'", title);
            }
            return where;
        }
        private String[][] formatResults(ResultSet rs){
            ArrayList<String[]> resultData = new ArrayList<String[]>();
            try{
                while(rs.next()){
                    String[] entry = {rs.getString("isbn"), rs.getString("title"), rs.getString("authors"), rs.getString("library_name"), rs.getString("shelf"), rs.getString("available_copies")};
                    resultData.add(entry);
                }
            }catch(SQLException e){
                System.out.println(e.getMessage());
            }
            if(resultData.size() == 0) return null;
            String[][] data = new String[resultData.size()][6];
            return resultData.toArray(data);
        }
        public void executeMemberInsert(String id, String first, String last, String dob, String gender) throws SQLException{
            Connection conn = makeConnection();
            String sqlString = createMemberInsertString(id, first, last, dob, gender);
            executeInsert(conn, sqlString);
        }
        private void executeInsert(Connection conn, String sqlStatement) throws SQLException{
            Statement stmt = conn.createStatement();
            stmt.execute(sqlStatement);
        }
        private String createMemberInsertString(String id, String first, String last, String dob, String gender){
            String insertString = String.format("INSERT INTO member VALUES(%s,'%s','%s','%s','%s')", id, first, last, dob, gender);
            return insertString;
        }
    }


    public class GUI{

        public class NotNumException extends Exception{
            public NotNumException(String s){
                super(s);
            }
        }
        public class BadDOBException extends Exception{
            public BadDOBException(String s){
                super(s);
            }
        }
        public class SelectGenderException extends Exception{
            public SelectGenderException(String s){
                super(s);
            }
        }
        public class NoFirstNameException extends Exception{
            public NoFirstNameException(String s){
                super(s);
            }
        }
        public class NoLastNameException extends Exception{
            public NoLastNameException(String s){
                super(s);
            }
        }
    
        JFrame frame;
        JPanel panel;
        public GUI(){
            frame = createInitialFrame();
            panel = new JPanel(new GridBagLayout());
        }
        private JFrame createInitialFrame(){
            JFrame frame = new JFrame("Fort Collins Library Book Search");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLayout(new BorderLayout());
            JPanel welcomeMessage = new JPanel();
            welcomeMessage.add(new JLabel("Welcome to the Fort Collins Library System"));
            JPanel bottomMessage = new JPanel();
            bottomMessage.add(new JLabel("Library Database System"));
            frame.add(welcomeMessage, BorderLayout.PAGE_START);
            frame.add(bottomMessage, BorderLayout.PAGE_END);
            return frame;
        }
        public void loadInitial(){
            frame.add(panel, BorderLayout.CENTER);
            createEnterMemberID();
            frame.setVisible(true);
        }
        private void createEnterMemberID(){
            panel.removeAll();
            GridBagConstraints gbc = createGBC();
            JLabel memberIDLabel = new JLabel("memberID");
            JTextField memberIDTextField = new JTextField(10);
            JLabel instructions = new JLabel("To start using the system please enter your member id");
            JLabel badID = new JLabel(" ");
            badID.setForeground(Color.RED);
            badID.setPreferredSize(new Dimension(200, 20));
            gbc.gridwidth = 3;
            gbc.gridx = 0;
            panel.add(instructions, gbc);
            
            gbc.gridwidth = 1;
            gbc.gridy = 1;
            gbc.gridx = 0;
            panel.add(memberIDLabel, gbc);
    
            gbc.gridx = 1;
            panel.add(memberIDTextField, gbc);
    
            gbc.gridx = 2;
            panel.add(badID, gbc);
    
            JButton enterButton = new JButton("Enter");
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 3;
            panel.add(enterButton, gbc);
    
            enterButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e){
                    try{
                        onlyDigits(memberIDTextField.getText());
                        if(validateMember(Integer.parseInt(memberIDTextField.getText()))) loadBookPanel();
                        else loadNewOrRedo();
                    }catch(NotNumException nne){
                        badID.setText("ID must only contain digits");
                    }
                }
            });
        }
        private GridBagConstraints createGBC(){
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;
            return gbc;
        }
        private void onlyDigits(String stringToCheck) throws NotNumException{
            String regex = "[0-9]+"; 
            Pattern p = Pattern.compile(regex); 
            if (stringToCheck == null) { 
                System.out.println("num null"); 
                throw new NotNumException("Not a Number");
            } 
            Matcher m = p.matcher(stringToCheck); 
            if (!m.matches()) throw new NotNumException("Not a Number");
	    }
        private boolean validateMember(int memberID){
            SQLRelay relay = new SQLRelay();
            try{
                relay.verifyMember(memberID);
                return true;
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
            return false;
        }
        public void loadBookPanel(){
            createSearchBook();
            frame.revalidate();
            frame.repaint();
        }
        public void createSearchBook(){
            panel.removeAll();
            GridBagConstraints gbc = createGBC();
    
            JLabel message = new JLabel("To search for a book enter the ISBN, Title, or Author");
    
            JLabel isbnLabel = new JLabel("ISBN:");
            JTextField isbnTextField = new JTextField(15);
    
            JLabel titleLabel = new JLabel("Title:");
            JTextField titleTextField = new JTextField(15);
    
            JLabel authorLabel = new JLabel("Author:");
            JTextField authorTextField = new JTextField(15);
    
            JButton searchButton = new JButton("Search");
    
            gbc.gridwidth = 2;
            panel.add(message, gbc);
    
            gbc.gridwidth = 1;
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(isbnLabel, gbc);
    
            gbc.gridx = 1;
            panel.add(isbnTextField, gbc);
    
            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(titleLabel, gbc);
    
            gbc.gridx = 1;
            panel.add(titleTextField, gbc);
    
            gbc.gridx = 0;
            gbc.gridy = 3;
            panel.add(authorLabel, gbc);
    
            gbc.gridx = 1;
            panel.add(authorTextField, gbc);
    
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 2;
            
            panel.add(searchButton, gbc);
    
            searchButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String isbn = isbnTextField.getText();
                    String title = titleTextField.getText();
                    String author = authorTextField.getText();
    
                    String[][] data = searchQuery(isbn, title, author);
                    if(panel.getComponentCount() > 8){
                        panel.remove(9);
                        panel.remove(8);
                    } 
                    setResultType(data, gbc);
                }
            });
        }
        private void setResultType(String[][] searchData, GridBagConstraints gbc){
            if (searchData == null) noSearchResults(gbc, "No Books Found");
            else if (!checkNoneAvail(searchData)) noSearchResults(gbc, "No Copies Available");
            else availableSearchResults(searchData, gbc);
        }
        public void availableSearchResults(String[][] searchData, GridBagConstraints gbc){
            String[] columnNames = {"ISBN", "Title", "Authors", "Library", "Shelf", "Available Copies"};
            DefaultTableModel tableModel = new DefaultTableModel(searchData, columnNames);
            JTable table = new JTable(tableModel);
            table.setPreferredScrollableViewportSize(new Dimension((int)(frame.getWidth()*.8), (int)(frame.getHeight()*.4)));

            JScrollPane scrollPane = new JScrollPane(table);
            gbc.gridy = 5;
            gbc.gridwidth = 4;
            panel.add(scrollPane, gbc);
            JButton done = new JButton("Done");
            gbc.gridy = 6;
            panel.add(done, gbc);
            done.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loadMemberIDpanel();
                }
            });
            frame.revalidate();
            frame.repaint();
        }
        public void loadMemberIDpanel(){
            createEnterMemberID();
            frame.revalidate();
            frame.repaint();
        }
        public void noSearchResults(GridBagConstraints gbc, String desc){
            JLabel message = new JLabel(desc);
            JButton done = new JButton("Done");
            gbc.gridwidth = 2;
            gbc.gridy = 5;
            panel.add(message, gbc);
            gbc.gridy = 6;
            panel.add(done, gbc);
            done.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loadMemberIDpanel();
                }
            });
            frame.revalidate();
            frame.repaint();
        }
        private boolean checkNoneAvail(String[][] searchData){
            for(int i = 0; i < searchData.length; i++){
                if(Integer.parseInt(searchData[i][5]) > 0) return true;
            }
            return false;
        }
        private String[][] searchQuery(String isbn, String title, String author){
            isbn = toNull(isbn);
            title = toNull(title);
            author = toNull(author);
            SQLRelay relay = new SQLRelay();
            String[][] data = relay.searchBooks(isbn, author, title);
            return data;		
        }
        private String toNull(String s){
            return (s.length() != 0) ? s : null;
        }
        private void loadNewOrRedo(){
            createNewOrRedo();
            frame.revalidate();
            frame.repaint();
        }
        private void createNewOrRedo(){
            panel.removeAll();
            GridBagConstraints gbc = createGBC();
            JLabel message = new JLabel("MemberID not found, create a new member or re-enter your MemberID");
            JButton newMember = new JButton("New Member");
            JButton tryAgain = new JButton("Try Again");
            gbc.gridwidth = 3;
            panel.add(message, gbc);
    
            gbc.gridwidth = 1;
            gbc.gridy = 1;
            gbc.gridx = 1;
            panel.add(newMember, gbc);
    
            gbc.gridx = 2;
            panel.add(tryAgain, gbc);
    
            newMember.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loadCreateMember();
                }
            });
    
            tryAgain.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                   loadMemberIDpanel();
                }
            });
    
        }
        public void loadCreateMember(){
            createNewMember();
            frame.revalidate();
            frame.repaint();
        }
        public void createNewMember(){
            panel.removeAll();
            GridBagConstraints gbc = createGBC();
    
            JLabel memberIDLabel = new JLabel("Member ID:");
            JTextField memberIDTextField = new JTextField(10);
            JLabel memberIDErrorLabel = new JLabel(" ");
            memberIDErrorLabel.setForeground(Color.RED);
            memberIDErrorLabel.setPreferredSize(new Dimension(200, 20));
    
            JLabel firstNameLabel = new JLabel("First Name:");
            JTextField firstNameTextField = new JTextField(10);
            JLabel lastNameLabel = new JLabel("Last Name:");
            JTextField lastNameTextField = new JTextField(10);
            JLabel nameErrorLabel = new JLabel(" ");
            nameErrorLabel.setForeground(Color.RED);
            nameErrorLabel.setPreferredSize(new Dimension(200, 20));
    
            JLabel lastNameErrorLabel = new JLabel(" ");
            lastNameErrorLabel.setForeground(Color.RED);
            lastNameErrorLabel.setPreferredSize(new Dimension(200, 20));
    
            JLabel dobLabel = new JLabel("Date of Birth:");
            JTextField dobTextField = new JTextField(10);
            JLabel dobErrorLabel = new JLabel(" ");
            dobErrorLabel.setForeground(Color.RED);
            dobErrorLabel.setPreferredSize(new Dimension(200, 20));
    
            JLabel genderLabel = new JLabel("Gender:");
            JLabel genderErrorLabel = new JLabel(" ");
            genderErrorLabel.setForeground(Color.RED);
            genderErrorLabel.setPreferredSize(new Dimension(200, 20));
    
            JRadioButton maleButton = new JRadioButton("Male");
            JRadioButton femaleButton = new JRadioButton("Female");
            ButtonGroup genderGroup = new ButtonGroup();
            genderGroup.add(maleButton);
            genderGroup.add(femaleButton);
            JButton submitButton = new JButton("Submit");
    
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(memberIDLabel, gbc);
            gbc.gridx = 1;
            panel.add(memberIDTextField, gbc);
            gbc.gridx = 2;
            panel.add(memberIDErrorLabel, gbc);
    
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(firstNameLabel, gbc);
            gbc.gridx = 1;
            panel.add(firstNameTextField, gbc);
            gbc.gridx = 2;
            panel.add(nameErrorLabel, gbc);
    
            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(lastNameLabel, gbc);
            gbc.gridx = 1;
            panel.add(lastNameTextField, gbc);
            gbc.gridx = 2;
            panel.add(lastNameErrorLabel, gbc);
    
            gbc.gridx = 0;
            gbc.gridy = 3;
            panel.add(dobLabel, gbc);
    
            gbc.gridx = 1;
            panel.add(dobTextField, gbc);
    
            gbc.gridx = 2;
            panel.add(dobErrorLabel, gbc);
    
            gbc.gridx = 0;
            gbc.gridy = 4;
            panel.add(genderLabel, gbc);
            gbc.gridx = 2;
            panel.add(genderErrorLabel, gbc);
    
            gbc.gridx = 1;
            JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            genderPanel.add(maleButton);
            genderPanel.add(femaleButton);
            panel.add(genderPanel, gbc);
    
            gbc.gridx = 0;
            gbc.gridy = 5;
            gbc.gridwidth = 5;
            gbc.anchor = GridBagConstraints.CENTER;
            panel.add(submitButton, gbc);
    
            submitButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    memberIDErrorLabel.setText(" ");
                    dobErrorLabel.setText(" ");
                    genderErrorLabel.setText(" ");
                    lastNameErrorLabel.setText(" ");
                    nameErrorLabel.setText(" ");
            
                    String memberID = memberIDTextField.getText();
                    String firstName = firstNameTextField.getText();
                    String lastName = lastNameTextField.getText();
                    String dob = dobTextField.getText();
                    String gender = maleButton.isSelected() ? "M" : "F";
                    try{
                        onlyDigits(memberID);
                        firstNameCheck(firstName);
                        lastNameCheck(lastName);
                        dobFormatCheck(dob);
                        genderCheck(maleButton.isSelected(), femaleButton.isSelected());
                        memberDatabaseInsert(memberID, firstName, lastName, dob, gender);
                        loadMemberAdded();
                    }catch(NotNumException nne){
                        memberIDErrorLabel.setText("Member ID must be a number");
                    }catch(BadDOBException bde){
                        dobErrorLabel.setText("Format yyyy-mm-dd");
                    }catch(SelectGenderException sge){
                        genderErrorLabel.setText(sge.getMessage());
                    }catch(NoLastNameException ne){
                        lastNameErrorLabel.setText(ne.getMessage());
                    }catch(NoFirstNameException ne){
                        nameErrorLabel.setText(ne.getMessage());
                    }
                }
            });
        }
        public void loadMemberAdded(){
            createMemberAdded();
            frame.revalidate();
            frame.repaint();
        }
        public void createMemberAdded(){
            panel.removeAll();
            GridBagConstraints gbc = createGBC();
            JLabel message = new JLabel("Member Added successfully to system");
            JButton continueButton = new JButton("Continue");
    
            panel.add(message, gbc);
            gbc.gridy = 1;
            panel.add(continueButton, gbc);
    
            continueButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e){
                    loadBookPanel();
                }
            });
        }
        public void memberDatabaseInsert(String id, String first, String last, String dob, String gender){
            SQLRelay relay = new SQLRelay();
            try{
                relay.executeMemberInsert(id, first, last, dob, gender);
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
        private void dobFormatCheck(String dob) throws BadDOBException{
            String regex = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dob);
            if(!m.matches()) throw new BadDOBException("Incorrect Date Format");
        }
        private void genderCheck(Boolean male, Boolean female) throws SelectGenderException{
            if(!male && !female) throw new SelectGenderException("Select a Gender");
        }
        private void lastNameCheck(String name) throws NoLastNameException{
            if(!nameCheck(name)) throw new NoLastNameException("Need to enter a name");
        }
        private void firstNameCheck(String name) throws NoFirstNameException{
            if(!nameCheck(name)) throw new NoFirstNameException("Need to enter a name");
        }
        private boolean nameCheck(String name){
            if(name.length() == 0) return false;
            return true;
        }

    }
    

    public static void main(String[] args) {
        Lab10Geisterfer lab = new Lab10Geisterfer();
        GUI gui = lab.new GUI();
        gui.loadInitial();
     }
}
