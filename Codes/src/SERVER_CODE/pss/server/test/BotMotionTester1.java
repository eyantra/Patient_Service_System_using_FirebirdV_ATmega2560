/*
 * =====================================================================================
 *
 *       Filename:  BotMotionTester1.java
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

package pss.server.test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import pss.configuration.Configure;
import pss.serialcomm.CommunicationAPI;
import pss.server.Bot;
import pss.server.Graph;
import pss.server.RequestHandler;

/**
 * Simulates bot by following instructions transmitted to it by the server
 */
public class BotMotionTester1 {

    /**
     * Debugging flag
     */
    public static final Boolean DEBUG = false;
    /**
     * Debugging flag
     */
    public static final Boolean mode1 = false;
    /**
     * Debugging flag
     */
    public static final Boolean mess_debug = true;

    /**
     * Prints the 8 bit message and all interpretations of the information encoded in the message
     * @param message Message of the bot
     */
    public static void printMess(Character message) {
        if (DEBUG) {
            System.out.println("Message is " + (int) message);
        }
        int mess_int = (int) message;
        int bot_id = mess_int / 32;
        int status = mess_int % 32;
        if (status == Graph.RIGHT) {
            if (mess_debug) {
                System.out.println("Server says: Bot #" + bot_id + " should move right " + status);
            }
        }
        if (status == Graph.LEFT) {
            if (mess_debug) {
                System.out.println("Server says: Bot #" + bot_id + " should move left " + status);
            }
        }
        if (status == Graph.STRAIGHT) {
            if (mess_debug) {
                System.out.println("Server says: Bot #" + bot_id + " should move straight " + status);
            }
        }
        if (status == Graph.BACKWARD) {
            if (mess_debug) {
                System.out.println("Server says: Bot #" + bot_id + " should move backward " + status);
            }
        }

    }

    /**
     * This main function sends an initial request to the server an then simulates the behaviour of bot number 1 as it carries out
     * the instructions to service the request as it receives them from the server
     * This test is done as follows<br/>
     * <p>
     * 1. Send an initial message to server indicating that patient 3 wants water. <br/>
     * 2.
     * </p>
     * Keep sending acks
     * @param args
     */
    public static void main(String[] args) {
        try {
            Configure.getInstance();
        } catch (IOException ex) {
            Logger.getLogger(BotMotionTester1.class.getName()).log(Level.SEVERE, null, ex);
        }
        CommunicationAPI capi = new CommunicationAPI(Configure.TEST_PORT1);
        capi.open();
        Boolean if_send = true;
        Character patient_mess = ((char) 3 * 32 + RequestHandler.WATER);
        //capi.send(Character.toString(patient_mess));
        int ack = Bot.bot1 * 32 + RequestHandler.ACK;
        char ack_message = (char) ack;
        //capi.send(Character.toString(ack_message));
        int inprogress = Bot.bot1 * 32 + RequestHandler.IN_PROGRESS;
        char inprogress_message = (char) inprogress;
        while (true) {
            if (DEBUG) {
                System.out.println("Bot motion tester1:");
            }
            Character inp_mess = null;
            if (DEBUG) {
                System.out.println(" getting next_char_in_buffer");
            }
            inp_mess = capi.next_char_in_buffer();
            if (DEBUG) {
                System.out.println("Got next_char_");
            }
            if (Bot.getIdMess(inp_mess) == Bot.bot1) {
                printMess(inp_mess);
                if (((int) inp_mess) % 32 == Graph.BOT_POLLING) {
                    if (if_send) {
                        capi.send(Character.toString(ack_message));
                        if_send = false;
                        if (DEBUG) {
                            System.out.println("sending an ack ");
                        }

                    } else {
                        capi.send(Character.toString(inprogress_message));
                        if_send = true;
                        if (DEBUG) {
                            System.out.println("sending in progress");
                        }

                    }
                }
            }

        }
    }
}
