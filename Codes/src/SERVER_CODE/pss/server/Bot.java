/*
 * =====================================================================================
 *
 *       Filename:  Bot.java
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

package pss.server;

import pss.server.database.DBHandler;
import pss.server.test.BotMotionTester1;
import pss.serialcomm.CommunicationAPI;

public class Bot {

    /**
     * Boolean flag for debugging purposes
     */
    public static final Boolean DEBUG = false;
    /**
     * Status flag for the bot. <br/> Bot is currently executing an instruction issued by the server
     */
    public static final int running = 0;
    /**
     * Status flag for the bot. <br/> Bot has finished executing  current instruction and is idle.
     */
    public static final int stationary = 1;
    /**
     * Status flag for the bot.<br/> Bot is obstructed while carrying out current instruction.
     */
    public static final int obstruction = 3;
    
    /** 
     * Boolean flag to indicate if the bot is currently servicing a request or not
     */
    boolean isInUse = false;

    /**
     * Integer id of a bot
     */
    int id;
    /**
     * Status of bot as given by status flags.
     * @see running
     * @see stationary
     * @see obstruction
     */
    int status;
    /**
     * Current position of the bot.
     * @see Position
     */
    Position currpos;
    DBHandler dbh;
    CommunicationAPI capi;
    int pid;
    Graph g = new Graph();
    /**
     * ID of the first bot
     */
    public static int bot1 = 0;
    /**
     * ID of the second bot
     */
    public static int bot2 = 1;

    /**
     * Is the bot in its Origional Starting Orientation. The origional orientation is fixed based on the bot ID.
     * @return True if it was in origional position. Flase otherwise.
     * @see Position
     */
    public Boolean isOriginalOrientation() {
        if (id == bot1) {
            if (currpos.orientation == Graph.SERVER_ID1_orientation) {
                System.out.println("Bot " + id + " is in correct orientation");
                return true;
            }
        } else {
            if (currpos.orientation == Graph.SERVER_ID2_orientation) {
                System.out.println("Bot " + id + " is in correct orientation");
                return true;
            }
        }
        System.out.println("Bot " + id + " is not in correct orientation");

        return false;
    }

    /**
     * This function checks if the bot is currently servicing a request or not.
     * @return True if it is <b> NOT </b> servicing a request. False otherwise.
     */
    public Boolean isBotIdle() {
        if (this.isInUse) {
            System.out.println("It is in use");
        }
        if (!this.isInUse) {
            System.out.println("It is not in use");
        }
        if ((isAtHome()) && (isOriginalOrientation()) && (!(isInUse))) {
            return true;
        }
        System.out.println("Returning false");
        return false;
    }

    /**
     * this function checks if the bot is at server (origional starting position).
     * @return True if bot is at the server; false otherwise.
     */
    public Boolean isAtHome() {
        if (id == bot1) {
            if (currpos.present == Graph.SERVER_ID1_position) {
                System.out.println("Bot " + id + " is at home");
                return true;
            }
        } else {
            if (currpos.present == Graph.SERVER_ID2_position) {
                System.out.println("bot " + id + " is at home");
                return true;
            }
        }
        System.out.println(" bot " + id + " is not at home");
        return false;
    }

    /**
     * this function checks if the position pos is safe to visit i.e. there willl not be any collision/deadlock if the
     * other bot also deceides the visit the node pos at the same time
     * @param pos Position that the bot is about to visit.
     * @return True if the position is safe false otherwise.
     * @see Position
     */
    public Boolean isSafePos(Position pos) {
        int mypos = currpos.present;
        int otherpos = pos.present;
        if (mypos == otherpos) {
            return false;
        } else {
            if ((mypos == 0 && otherpos == 3) || (mypos == 3 && otherpos == 0)) {
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * Bot Object Constructor
     * @param dbis Database Handler
     * @param id Unique ID for the bot.
     * @param capi Comunication API Object.
     * @see CommunicationAPI
     * @see DBHandler
     */
    public Bot(DBHandler dbis, int id, CommunicationAPI capi) {
        this.dbh = dbis;
        this.capi = capi;
        this.id = id;
        if (id == bot1) {
            pid = Graph.SERVER_ID1;
            currpos = new Position(Graph.SERVER_ID1_position, Graph.SERVER_ID1_orientation);
        }
        if (id == bot2) {
            pid = Graph.SERVER_ID2;
            currpos = new Position(Graph.SERVER_ID2_position, Graph.SERVER_ID2_orientation);
        }
        this.isInUse = false;
        this.status = stationary;
    }

    /**
     * All messages between bot and server are 8 bits long. In Each message the ID of the bot which
     * sent the message and the message itself is encoded.
     * @param mess 8 bit message.
     * @return bot ID of the bot which sent the message.
     */
    public static int getIdMess(char mess) {
        int int_mess = (int) mess;
        return int_mess / 32;
    }

    /**
     * given a position x, set the bot's current position to be x
     * @param x
     * @see Position
     */
    public void setPosition(Position x) {
        this.currpos = x;
    }

    /**
     * Send an instruction to the bot to go forward upto the next cross (intersection) and update the new position of the bot.
     */
    public void gotoNextCross() {
        //change bot pos.
        if (DEBUG) {
            printBotPos();

            System.out.println("going straight");
        }

        this.currpos = g.Straight(currpos);
        //and send command to BOT.
        String t = Character.toString(Utils.straight_message(id));
        capi.send(t);
        BotMotionTester1.printMess(Utils.straight_message(id));
    }

    /**
     * For debugging purposes <br/>
     * Print the current position of the bot
     */
    public void printBotPos() {
        System.out.println("Bot id: " + id + "the position is " + currpos.present + "orientation is " + currpos.orientation);
    }

    /**
     * Sends an instruction to the bot to turn right and set the position of the bot to the new position after turning right
     */
    public void turnRight() {
        //send command to bot.
        if (DEBUG) {
            printBotPos();
            System.out.println("turning right");
        }
        this.currpos = g.Right_turn(this.currpos);
        String t = Character.toString(Utils.right_message(id));
        capi.send(t);
        BotMotionTester1.printMess(Utils.right_message(id));
    }

    /**
     * Sends an instruction to the bot to turn back and set the position of the bot to the new position after turning back
     */
    public void turnBack() {
        if (DEBUG) {
            printBotPos();
            System.out.println("turn back message is " + (int) Utils.back_message(id));
        }
        String t = Character.toString(Utils.back_message(id));
        this.currpos = g.Back(this.currpos);
        capi.send(t);
        BotMotionTester1.printMess(Utils.back_message(id));
    }

    /**
     * Sends an instruction to the bot to turn left and set the position of the bot to the new position after turning left
     */
    public void turnLeft() {
        printBotPos();
        if (DEBUG) {
            System.out.println("turning left");
        }
        this.currpos = g.Left_turn(this.currpos);
        String t = Character.toString(Utils.left_message(id));
        capi.send(t);
        BotMotionTester1.printMess(Utils.left_message(id));

    }
}
