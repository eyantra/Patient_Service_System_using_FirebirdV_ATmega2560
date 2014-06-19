/*
 * =====================================================================================
 *
 *       Filename:  Configure.java
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

package pss.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This is a standard class in most Java programs for storing and retreiving parameters and values which would have
 * otherwise been hardcoded. This class reads a text file config.properties which is simply a list of paramenters and their
 * properties. Everywhere in the code these parameters are accessed through this class only. If any of these
 * paraameters have to be changed we only need to do it in this tex file. 
 * @author ashish, rohit
 */
public class Configure {
    /**
     * Address of Serial USB PORT on server to which the Zigbee module has been connected<br/>
     * <p><b>Example : </b> /dev/tty0</p>
     */
    public static String ZIGBEE_PORT;
    /**
     * Username for accessing MySQL database
     */
    public static String DB_UN;
    /**
     * Password for accessing MySQL database
     */
    public static String DB_PASS;
    /**
     * Address of the MySQL JDBC driver.
     */
    public static String DB_DRIVER;
    /**
     * URL of the SQL database
     */
    public static String DB_URL;

    /**
     * Username for the SMS Gateway
     */
    public static String SMS_UN;
    /**
     * Password for the SMS Gateway
     */
    public static String SMS_PASS;
    /**
     * Message to be sent by SMS
     */
    public static String SMS_MSG;
    /**
     * Phone Number to which the SMS must be sent.
     */
    public static String SMS_NUM;
    /**
     * Number of serving (nurse) bots
     */
    public static int NUM_BOTS;
    /**
     * Flag indicating whether the existing database should be completely cleared and a new one
     * created from start. Can be true or false
     */
    public static boolean CLEAR_DB;


    /**
     * USB Serial Port used for Simulating Bot 1 during testing using simulation . Same as PORT above.
     */
    public static String TEST_PORT1;
    /**
     * USB Serial Port used for Simulating Bot 2 during testing using simulation. Same as PORT above.
     */
    public static String TEST_PORT2;
    static Configure instance = null;

    /**
     * Initialization
     * @return
     * @throws IOException
     */
    public static synchronized Configure createInstance() throws IOException{
        if(instance == null){
            instance = new Configure();
            setValues(loadProperties());
        }
        return  instance;
    }

    /**
     * Get an instance
     * @return
     * @throws IOException
     */
    public static Configure getInstance() throws IOException{
        if(instance==null){
            createInstance();
        }
        return instance;
    }

    /**
     * Set values of a property
     * @param p
     * @see Properties
     */
    public static void setValues(Properties p){
        ZIGBEE_PORT = p.getProperty("zigbee_port");
        DB_DRIVER = p.getProperty("db_driver");
        DB_PASS = p.getProperty("db_password");
        DB_UN = p.getProperty("db_username");
        DB_URL = p.getProperty("db_url");
        NUM_BOTS = Integer.parseInt(p.getProperty("num_bots"));
        SMS_UN = p.getProperty("sms_un");
        SMS_PASS = p.getProperty("sms_pass");
        SMS_MSG = p.getProperty("sms_msg");
        CLEAR_DB = Boolean.parseBoolean(p.getProperty("clear_db"));
        SMS_NUM=p.getProperty("sms_num");
        TEST_PORT1 = p.getProperty("test_port1");
        TEST_PORT2 = p.getProperty("test_port2");
    }

    /**
     * Loads properties and values from configuration file
     * @return
     * @throws IOException
     */
    public static Properties loadProperties() throws IOException{
        Properties prop = new Properties();
        prop.load(new FileInputStream("config/config.properties"));
        return prop;
    }
}
