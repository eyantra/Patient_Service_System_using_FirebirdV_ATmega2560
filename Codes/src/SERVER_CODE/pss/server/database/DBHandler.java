/*
 * =====================================================================================
 *
 *       Filename:  DBHandler.java
 *
 *				 Date:  31st March, 2010
 *
 *        Version:  2.1
 *       Revision:  2.1
 *       Compiler:  javac
 *
 *        Authors:  Pritish Kamath, pritish.kamath@iitb.ac.in
 *						  Rohit Saraf	 , rohitsaraf@iitb.ac.in
 *						  Ashish Mathew , ashishmathew@iitb.ac.in
 *						  Vivek Madan	 , vivekmadan@iitb.ac.in
 *
 *        Company:  IIT Bombay
 *		  Copyright:  ERTS Lab, IIT Bombay
 *
 * =====================================================================================
 */

package pss.server.database;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import pss.configuration.Configure;

/**
 * Handler functions for interaction with MySQL database
 */
public class DBHandler {

    /*
     * System dependant parameters for setting up database connection
     */
    private static String driver_path = null;
    private static Connection conn = null;
    private static Statement stmt = null;
    private static String url = null;
    private static String username = null;
    private static String password = null;

    /**
     * @return List of commands to create tables.
     */
    private List<String> getCreateCommands() {
        List<String> a = new ArrayList<String>();
        a.add("create database erts");
        a.add("use erts");
        a.add("create table patient(patient_ID INT,Pos INT,primary key(patient_ID))");
        a.add("create table request(request_ID INT,patient_ID INT,item char,status char(20),primary key (request_ID),foreign key (patient_ID) references patient)");
        a.add("create table assignment(request_ID INT,bot_ID INT,foreign key (request_ID) references request)");

        return a;
    }

    private List<String> getDeleteCommands() {
        List<String> a = new ArrayList<String>();
        a.add("use erts");
        a.add("drop table assignment");
        a.add("drop table request");
        a.add("drop table patient");
        a.add("drop database erts");
        return a;
    }

    /**
     * Safely close database connection
     */
    public void closeConnection() {
        try {
            conn.close();
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    /**
     * <p>Create a connection to the database the following parameters must be filled in here.</p><br/>
     *  (1) Path to JAVA JDBC Driver<br/>
     *  (2) URL to MySQL database<br/>
     *  (3) Username to access MySQL database<br/>
     *  (4) Password to access MySQL database<br/>
     */
    public void createConnection() {
        try {
            Configure.getInstance();
        } catch (IOException ex) {
            Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        driver_path = Configure.DB_DRIVER;
        url = Configure.DB_URL;
        username = Configure.DB_UN;
        password = Configure.DB_PASS;
        try {
            Class.forName(driver_path).newInstance();
            conn = DriverManager.getConnection(url, username, password);
            stmt = conn.createStatement();
            if (conn == null) {
                System.out.println("WARNING : No connection set up to create database");
            }

        } catch (ClassNotFoundException ex) {
            System.err.println(ex.getMessage());
        } catch (IllegalAccessException ex) {
            System.err.println(ex.getMessage());
        } catch (InstantiationException ex) {
            System.err.println(ex.getMessage());
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Erases all existing tables and creates a fresh empty tables
     */
    public void createDatabase() {
        List<String> cmds = getCreateCommands();
        for (String cmd : cmds) {
            this.executeUpdate(cmd);
        }
    }

    /**
     * Completely delete the existing database as well as all the information stored in it
     */
    public void deleteDatabase() {
        List<String> cmds = getDeleteCommands();
        for (String cmd : cmds) {
				try{
            	this.executeUpdate(cmd);
				}catch(Exception e){}
        }
    }

    /**
     * Execute an SQL Query on the database
     * @param cmd   SQL Query
     * @return      ResultSet
     * @see ResultSet
     */
    public ResultSet executeStatement(String cmd) {
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(cmd);

        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        if (rs == null) {
            System.out.println("Warning execution unsucessful");
        }
        return rs;
    }

    /**
     * Execute SQL Update on the database
     * @param cmd SQL Update statement
     */
    public void executeUpdate(String cmd) {
        try {
            stmt.executeUpdate(cmd);
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Prints the resultSet in a tabular format to a specified printStream
     * @param rs    ResultSet
     * @param ps    PrintStream
     * @see ResultSet
     */
    private void printResultSet(ResultSet rs, PrintStream ps) {
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            String head = "";
            int colCount = rsmd.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                head += rsmd.getColumnName(i) + "\t";
            }
            ps.println(head);
            while (rs.next()) {
                String temp = "";
                for (int i = 1; i <= colCount; i++) {
                    temp = temp + "\t" + rs.getString(i);
                }
                ps.println(temp);
            }
        } catch (SQLException e) {
            ps.println(e);
        }
    }
    /**
     * Debugging flags
     */
    public static final Boolean DEBUG = false;

    /**
     * Add a patient record
     * @param patient_ID    Patient ID
     * @param Pos           Position on graph (Room number)
     * @return              True if update is successful false otherwise
     */
    public boolean addPatient(int patient_ID, int Pos) {
        if (DEBUG) {
            System.out.println("Adding patient");
        }
        try {
            PreparedStatement ps = conn.prepareStatement("insert into patient(patient_ID,Pos) values(?,?)");
            ps.setInt(1, patient_ID);
            ps.setInt(2, Pos);
            ps.executeUpdate();
            if (DEBUG) {
                System.out.println("Added patient");
            }

            return true;
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Could not add patient");
            return false;
        }
    }

    /**
     * Deletes patient record
     * @param   id Patient ID
     * @return  True if update is successful false otherwise
     */
    public boolean deletePatient(int id) {
        try {
            PreparedStatement ps = conn.prepareStatement("delete from patient where patient_ID=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Add record of a request
     * @param request_id    Unique Request ID
     * @param patient_id    Patient ID
     * @param item          Item requested for
     * @param status        Status of request
     * @return              True if update is successful false otherwise
     */
    public boolean addRequest(int request_id, int patient_id, String item, String status) {
        try {
            PreparedStatement ps = conn.prepareStatement("insert into request(request_ID,patient_ID,item,status) values(?,?,?,?)");
            ps.setInt(1, request_id);
            ps.setInt(2, patient_id);
            ps.setString(3, item);
            ps.setString(4, status);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Delete record of a request
     * @param request_id    Request ID
     * @return              True if update is successful false otherwise
     */
    public boolean deleteRequest(int request_id) {
        try {
            PreparedStatement ps = conn.prepareStatement("delete from request where request_ID=?");
            ps.setInt(1, request_id);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Update status of a request
     * @param request_id    Request ID
     * @param status        New status of request
     * @return              True if update is successful false otherwise
     */
    public boolean updateRequestStatus(int request_id, String status) {
        try {
            PreparedStatement ps = conn.prepareStatement("update request set status =? where request_ID=?");
            ps.setString(1, status);
            ps.setInt(2, request_id);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Add record of bot being assigned to service a particular request
     * @param request_id    Request ID
     * @param bot_ID        Bot ID
     * @return              True if update is successful false otherwise
     */
    public boolean addAssignment(int request_id, int bot_ID) {
        try {
            PreparedStatement ps = conn.prepareStatement("insert into assignment(request_ID,bot_ID) values(?,?)");
            ps.setInt(1, request_id);
            ps.setInt(2, bot_ID);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Delete record of an assignment
     * @param id                BotID or Request ID
     * @param is_request_id     True if parameter id is requestID false if it is botID
     * @return                  True if update is successful false otherwise
     */
    public boolean deleteAssignment(int id, boolean is_request_id) {
        try {
            PreparedStatement ps;
            if (is_request_id) {
                ps = conn.prepareStatement("delete from assignment where request_ID=?");
            } else {
                ps = conn.prepareStatement("delete from assignment where bot_ID=?");
            }
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Get next request based on FIFIO scheduling policy
     * @return request ID of next request that has to be assigned to a bot for servicing or -1 if no pending requests.
     */
    public int getNextRequestFIFO() {
        try {
            PreparedStatement ps;
            ps = conn.prepareStatement("select request_ID from request where status=? order by request_ID ASC");
            ps.setString(1, "pending");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return -1;
            }
        } catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }

    /**
     * Gets the request assigned to a particular  bot
     * @param bot_ID    Bot ID
     * @return          Request ID or -1 if no such bot exists
     */
    public int getAssignedRequest(int bot_ID) {
        try {
            PreparedStatement ps;
            ps = conn.prepareStatement("select request_ID from assignment where bot_ID=?");
            ps.setInt(1, bot_ID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return -1;
            }
        } catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }

    /**
     * Get ID of patient who made this request
     * @param req_id    Request ID
     * @return          Patient ID or -1 if no such request is present in the system
     */
    public int getPatientOfRequest(int req_id) {
        try {
            PreparedStatement ps;
            ps = conn.prepareStatement("select patient_ID from request where request_ID=?");
            ps.setInt(1, req_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return -1;
            }
        } catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }

    /**
     * Simple main to test correctness of any of the functions above
     * @param args
     * @throws SQLException
     */
    public static void main(String[] args) throws SQLException {


        DBHandler dbh = new DBHandler();
        dbh.createConnection();

        System.out.println("Initializing database...");
        if (true) {
            try {
                dbh.deleteDatabase();
            } catch (Exception e) {
                System.out.println("No database to delete");
            }
        }
        try {
            dbh.createDatabase();
        } catch (Exception e) {
            dbh.executeUpdate("use erts");
            System.out.println("Reading Existing Database...");
        }


        String a[] = new String[1];
        a[0] = "request";

        dbh.addRequest(1, 5, "1", "pending");
        dbh.addRequest(2, 5, "1", "pending");
        dbh.addRequest(3, 5, "1", "pending");
        dbh.addAssignment(1, 3);
        dbh.addPatient(2, 5);
        ResultSet rs = dbh.executeStatement("select * from request");
        dbh.printResultSet(rs, System.out);

        System.out.println(dbh.getAssignedRequest(3));
        System.out.println(dbh.getNextRequestFIFO());
        System.out.println(dbh.getPatientOfRequest(3));
        dbh.closeConnection();
    }
}
