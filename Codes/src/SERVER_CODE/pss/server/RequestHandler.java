/*
 * =====================================================================================
 *
 *       Filename:  RequestHandler.java
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

/**
\mainpage
See the file list to find the bot_code files.
<h2>
PROJECT DESCTIPTION
</h2>
The project aims at helping the patients and
hostipal management by providing automated
service bots, who can be called by the patient
simply by using a cheap TV remote, and who
provide a guarantee of no-deadlock/race
conditions. The server uses a full fledged database
(MySQL) to maintain the queue and manages the
bot wisely using a perfectly scalable design. The
bots are fair to all the patients. The bot notifies the
guards if it is blocked by an SMS.
<br>
* For more details, see the presentation
<br>
<h2>
TEAM MEMBERS
</h2>
<table>
<tr><td class="indexvalue">Pritish Kamath</td></tr>
<tr><td class="indexvalue">Rohit Saraf</td></tr>
<tr><td class="indexvalue">Ashish Mathew</td></tr>
<tr><td class="indexvalue">Vivek Madan</td></tr>
</table>

<h2>
Execution Instructions (Ubuntu)
</h2>
<p>
To execute the code for the first time run the
install script as
<p>
<b>	sudo ./install</b><br>
(requires proxy settings)<br>
This will install all the dependencies and
drivers required for the project and generate
a Makefile.
<br><b>Note<b> :
1.The properties file config/config.properties might require a
change. (e.g. Zigbee COM port, Database settings)
<br><br>
Then
<table>
<tr><td class="indexkey">To compile the server code</td> <td class="indexkey">make server</td></tr>
<tr><td class="indexkey">To run the server code</td>  <td class="indexkey">make run</td></tr>
<tr><td class="indexkey">To compile and program the serving bot no 1.</td>  <td class="indexkey"> make bot </td></tr>
<tr><td class="indexkey">To compile and program the serving bot no 2.</td>  <td class="indexkey"> make bot1 </td></tr>
<tr><td class="indexkey">To compile and program the patient_IR bot</td>  <td class="indexkey"> make patient </td></tr>
<tr><td class="indexkey">To clean </td><td class="indexkey"> make clean </td></tr>
<tr><td class="indexkey">To open the documentation </td><td class="indexkey"> make doc </td></tr>
</table>
*/

package pss.server;

import java.io.IOException;
import pss.server.database.DBHandler;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import pss.configuration.Configure;
import pss.serialcomm.CommunicationAPI;
import pss.server.scheduling.PollingThread;

/**
 * The main method of this class listens for the request from patients and bots and performs the neccesary book-keeping
 * (Adding/Modifying database).
 * Then, in case of patients request, it assigns them to a free bot if such a bot is available.
 * When bot finished serving a patient, it returns to the server and statys there until it gets another request from the server.
 * In case of bot's response, server computes the next action bot has to perform and send the appropriate message.
 */
public class RequestHandler {

    public static final Boolean DEBUG = false;
    public static int no_bots = 0;
    /** Database Handler as defined in DBHandler
     *  @see DBHandler
     */
    DBHandler dbh;
    /**
     * A map between bot id and Bot object
     */
    static Map<Integer, Bot> botList = new HashMap<Integer, Bot>();
    /**
     * Floor lay-out
     * @see Graph
     */
    static Graph g = new Graph();
    /**
     * Xbee functions including for sending and reciving messages is implemented in this class
     * @see CommunicationAPI
     */
    public static CommunicationAPI capi;

    /**
     * This constructor initializes database, opens the communication interface and call functions
     * to add patients into the database and bots into local variables.
     * @see DBHandler
     * @see CommunicationAPI
     */
    public RequestHandler() {
        dbh = new DBHandler();
        dbh.createConnection();
        try {
            Configure.getInstance();
        } catch (IOException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Could not create connection");
        }

        boolean clearDatabase = Configure.CLEAR_DB;
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

        if (DEBUG) {
            System.out.println("Opening the Communication API...");
        }
        capi = new CommunicationAPI(Configure.ZIGBEE_PORT);
        capi.open();
        if (DEBUG) {
            System.out.println("Creating bot objects...");
        }
        create_bots(this.dbh);
        if (DEBUG) {
            System.out.println("Creating patients objects...");
        }
        create_patients(this.dbh);
    }

    /**
     * Adds patients to the database using the handler function
     * @param db Database Handler
     * @see DBHandler
     */
    private void create_patients(DBHandler db) {
        if (DEBUG) {
            System.out.println("Adding patient info...");
        }
        this.dbh.addPatient(2, 5);
        this.dbh.addPatient(3, 9);
        this.dbh.addPatient(4, 13);
        this.dbh.addPatient(5, 10);
        this.dbh.addPatient(6, 6);
        this.dbh.addPatient(7, 1);
        if (DEBUG) {
            System.out.println("Added Patient");
        }
    }

    /**
     * Create a bot object and add it into local hashmap botList if a bot with that id is not already present
     * @param db
     * @see DBHandler
     */
    private void create_bots(DBHandler db) {
        int first_bot_id = Bot.bot1;
        Bot firstbot = new Bot(db, first_bot_id, capi);
        if (!botList.containsKey(first_bot_id)) {
            botList.put(first_bot_id, firstbot);
        }
        if (no_bots == 2) {
            int second_bot_id = Bot.bot2;
            Bot secondbot = new Bot(db, second_bot_id, capi);
            if (!botList.containsKey(second_bot_id)) {
                botList.put(second_bot_id, secondbot);
            }
        }
    }

    /**
     * Sends a polling message to the next bot. This message requests the bot to tell the server it's current status.
     * This will keep running and will recieve commands from Communication API in the form of characters.
     * It will decode the command in the following manner.First three bits denote the id of the sender.
     * .If ID is either 0 or 1, its a bot else it is a patient
     *  In case of bot, the control is transfered to execute_bot_command otherwise the control is transfered to execute_patient_request
     */
    public static void main(String args[]) {
        try {
            Configure.getInstance();
        } catch (IOException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        no_bots = Configure.NUM_BOTS;
        RequestHandler rh = new RequestHandler();
        PollingThread poll = new PollingThread(capi);
        Character patient_mess1 = 6 * 32;
        System.out.println("Executing patient request...");
        while (true) {
            Character comm = poll.poll_next(PollingThread.bot_or_patient);
            if (DEBUG) {
                System.out.println("Got reply");
            }
            if (PollingThread.bot_or_patient == PollingThread.poll_bot) {
                rh.execute_bot_Command(comm);
                System.out.println("Got message " + Integer.toBinaryString((int) comm) + " from serving bot");
                if (poll.poll_id == poll.number_bots - 1) {
                    PollingThread.bot_or_patient = PollingThread.poll_patient;
                }
            } else if (PollingThread.bot_or_patient == PollingThread.poll_patient) {
                rh.execute_patient_request(comm);
                System.out.println("Got message " + Integer.toBinaryString((int) comm) + " from patient bot");
                PollingThread.bot_or_patient = PollingThread.poll_bot;
            }
            for (int i = 0; i < 2000; i++) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                }
            }
        }
    }
    /**
     * Flag for returning the status of the bot. Tells if some external object is obstructing the path of bot
     */
    public static final int BLOCKED = 0;
    /**
     * Flag for returning the status of the bot. Tells if the previous instruction assigned to the bot is complete.
     */
    public static final int ACK = 1;
    /**
     * Flag for returning the status of the bot. Tells if the previous instruction is still being executed.
     */
    public static final int IN_PROGRESS = 2;

    /**
     * Command message has the bot id encoded in the first three bits and message in rest of the firve bits <br/>
     * We have maintained the current position and the the patient it is searching.<br/>
     * Based on the status message returned from the bot following actions are performed<br/>
     * BLOCKED : send a command to bot to start a buzzer.<br/>
     * ACK : Compute the next immediate hop to be taken by the bot and send the command for that. If, bot is at the destination then start return trip to the server<br/>
     * IN_PROGRESS : Don't do anything<br/>
     * If the bot has reached the server after serving the request, then assign it a new request if available else<br/>
     * If it is not at the server then search for the action(LEFT,RIGHT,STRAIGHT) the bot should take.<br/>
     * If the bot is at the destination then signal that the request is completed and start returning to the server<br/>
     * If the action is right then ask the bot to turn right<br/>
     * If the action is left then ask the bot to turn left<br/>
     * If the action is straight, then ask the bot to go straight<br/>
     * If the action is turn_back then ask to bot to turn back<br/>
     * If the bot is in middle of executing the previous instruction then do nothing<br/>
     * @param command message from bot
     */
    public void execute_bot_Command(char command) {
        int comm_int = (int) command;
        int bot_id = comm_int / 32;
        int request = comm_int % 32;
        switch (request) {
            case BLOCKED:
                System.out.println("Patient is blocked ... SENDING SMS");
                try {
                    Runtime.getRuntime().exec("python send-sms.py " + Configure.SMS_NUM + " " + Configure.SMS_MSG);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                bot_blocked(bot_id);
                break;
            case ACK:
                System.out.println("Bot has completed its previous request");
                Bot x = botList.get(bot_id);
                Bot other_bot = botList.get((bot_id + 1) % no_bots);
                Position pos = x.currpos;
                if (x.isAtHome() && x.isOriginalOrientation()) {
                    System.out.println("Bot is idle");
                    assign_if_request_available(bot_id);
                } else {
                    System.out.println("Patient being served is " + x.pid);
                    int action = g.search(x.pid, pos);
                    System.out.println("Action is " + action);
                    boolean finish = true;
                    while (finish) {
                        switch (action) {
                            case Graph.NOPATH:
                                System.out.println("There is no path from this vertex");
                                break;
                            case Graph.FINISH:
                                System.out.println("Bot is in patient room");
                                request_completed(bot_id);
                                action = g.search(x.pid, pos);
                                finish = false;
                                break;
                            case Graph.LEFT:
                                System.out.println("Bot should turn left");
                                if (no_bots == 2) {
                                    if (!(other_bot.isSafePos(g.Left_turn(pos)))) {
                                        break;
                                    }
                                }
                                x.turnLeft();
                                finish = false;
                                break;
                            case Graph.RIGHT:
                                if (no_bots == 2) {
                                    if (!(other_bot.isSafePos(g.Right_turn(pos)))) {
                                        break;
                                    }
                                }
                                x.turnRight();
                                finish = false;
                                break;
                            case Graph.STRAIGHT:
                                if (no_bots == 2) {
                                    if (!(other_bot.isSafePos(g.Straight(pos)))) {
                                        break;
                                    }
                                }

                                x.gotoNextCross();
                                finish = false;
                                break;
                            case Graph.BACKWARD:
                                if (no_bots == 2) {
                                    if (!(other_bot.isSafePos(g.Left_turn(pos)))) {
                                        break;
                                    }
                                }

                                x.turnBack();
                                finish = false;
                                break;
                        }
                    }
                }
                break;
            case IN_PROGRESS:
                break;
        }
    }

    /**
     * Sends a command to the bot with id bot_id to start a buzzer
     * @param bot_id
     */
    public void bot_blocked(int bot_id) {
        try {
            Configure.getInstance();
        } catch (IOException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Sends a command to the bot with id bot_id to stop the buzzer
     * @param bot_id
     */
    public void buzzer_off(int bot_id) {
    }

    /**
     *  When the bot is free, search if there is any pending request in the database. If more than one request are pending use FIFO and assign that request to the bot with id bot_id
     * If no such request are pending then set the use_or_not flag of bot to be false
     * @param bot_id bot which is free
     */
    public void assign_if_request_available(int bot_id) {
        if (DEBUG) {
            System.out.println("Checking for pending requests...");
        }
        Bot x = botList.get(bot_id);
        int request_id = dbh.getNextRequestFIFO(); 
        if (request_id == -1) {
            if (DEBUG) {
                System.out.println("Setting bot to \"not is use\" mode...");
            }
            x.isInUse = false;
            return;
        } else {
            assign_bot_req(bot_id, request_id);
            if (DEBUG) {
                System.out.println("Setting bot to \"in use\" mode...");
            }
            x.isInUse = true;
        }
    }

    /**
     * Assign request to the bot <br/>
     * Set the use_or_not bit of the bot to be true. <br/>
     * Make the bot turn by 180 degree <br/>
     * Set the use_or_not bit of the bot to be true <br/>
     * Set the pid field of the bot to the id of the patient who made the request <br/>
     * Add the request id,bot id tuple to the assignment table of the database and update the status of the request in the database to serving <br/>
     * @param bot_id bot which is free
     * @param req_id pending request
     */
    public void assign_bot_req(int bot_id, int req_id) {
        Bot x = botList.get(bot_id);
        if (DEBUG) {
            System.out.println("Setting bot to \"in use\" mode");
        }
        x.isInUse = true;
        x.turnBack();
        x.pid = dbh.getPatientOfRequest(req_id);
        try {
            Configure.getInstance();
        } catch (IOException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            Runtime.getRuntime().exec("python send-sms.py " + Configure.SMS_NUM + " " + "Bot #" + bot_id + " is going to server the patient #" + (x.pid - 1));
        } catch (IOException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        dbh.addAssignment(req_id, bot_id);
        dbh.updateRequestStatus(req_id, "serving");
    }

    /**
     * Request is finished <br/>
     * Delete the request bot was serving from the assignment table <br/>
     * Update its status in the request table to done. <br/>
     * Set the destination of the bot to server <br/>
     * @param bot_id
     */
    public void request_completed(int bot_id) {
        int request_id = dbh.getAssignedRequest(bot_id);
        int patient_id = dbh.getPatientOfRequest(request_id);
        try {
            Configure.getInstance();
        } catch (IOException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        dbh.deleteAssignment(request_id, true);
        dbh.updateRequestStatus(request_id, "done");
        Bot x = botList.get(bot_id);
        if (bot_id == Bot.bot1) {
            x.pid = Graph.SERVER_ID1;
        } else if (bot_id == Bot.bot2) {

            x.pid = Graph.SERVER_ID2;
        }
    }

    /**
     * Integer representing request for water.
     */
    public static final int WATER = 0;
    /**
     * Integer representing request for medicine.
     */
    public static final int MEDICINE = 1;
    /**
     * Global counter to assign unique request ID's
     */
    public static int no_request = 0;

    /**
     * This function returns a unique number as request ID each time it is called
     * @return New Request ID
     */
    public int get_new_request_id() {
        return no_request++;
    }

    /**
     * Scans the BotList hashmap for an free bot. <br/>
     * @return If any free bot is present then return its ID <br/> If no such bot is present then return -1
     */
    public int get_a_free_bot() {
        if (DEBUG) {
            System.out.println("Searching for a free bot...");
        }
        Iterator<Entry<Integer,Bot>> it = botList.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Integer,Bot> t = (Entry<Integer,Bot>) it.next();
            if (((Bot) t.getValue()).isBotIdle()) {
                System.out.println("Bot " + ((Bot) t.getValue()).id + "  is free");
                return ((Bot) t.getValue()).id;
            } else {
                if (((Bot) t.getValue()).isInUse) {
                    System.out.println(((Bot) t.getValue()).id + "is in use");
                } else {
                    System.out.println(((Bot) t.getValue()).id + " is neither in use nor idle");
                }
                continue;
            }
        }
        System.out.println("No bot is free");
        return -1;
    }

    /**
     * Prints ID of patient who issued the command for debugging purposes
     * @param comm Command issued by patient (8 bits) typecasted as a char
     */
    public void print_patient_command(char comm) {
        int command = (int) comm;
        if (DEBUG) {
            System.out.println("Patient is " + comm / 32);
        }
    }
    public static int request_absent = 127;

    /**
     * This function processes the request received from the patient in the form of a char <br/>
     * Extracts the information about what item was requested for from the last 5 bits of comm. <br/>
     * Obtains a new unique id for this particular request <br/>
     * Adds a new entry to the request table containing request id, patient_id,item requested for and the status of the request as pending <br/>
     * Also check if a bot is free. If so then the id of the free bot is returned and is assigned to this request <br/>
     * Otherwise the function exits. <br/>
     * @param comm character encoding request from the patient
     */
    public void execute_patient_request(char comm) {
        System.out.println("Patient request code is " + (int) comm);
        print_patient_command(comm);
        int comm_int = (int) comm;
        if (comm_int == request_absent) {
            return;
        }
        int patient_id = comm_int / 32;


        System.out.println("Patiend id is " + patient_id);
        int request = comm_int % 32;
        int request_id = get_new_request_id();
        dbh.addRequest(request_id, patient_id, Integer.toString(request), "pending");
        int free_bot = get_a_free_bot();
        if (free_bot != -1) {
            System.out.println("request for patient" + patient_id + "assigned to bot" + free_bot);
            assign_bot_req(free_bot, request_id);
        }
    }
}
