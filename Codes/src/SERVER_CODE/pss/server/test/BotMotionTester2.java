/*
 * =====================================================================================
 *
 *       Filename:  BotMotionTester2.java
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

import pss.serialcomm.CommunicationAPI;
import pss.server.Graph;
import pss.server.RequestHandler;

/**
 *
 * @author ashish
 */
public class BotMotionTester2 {

    public static final Boolean DEBUG = false;
    public static final Boolean mode1 = false;
    public static final Boolean mess_debug = true;

    public static void print_mess(Character message) {
        if (DEBUG) {
            System.out.println("Message is " + (int) message);
        }
        int mess_int = (int) message;
        int bot_id = mess_int / 32;
        int instruction = mess_int % 32;
        if (instruction == Graph.RIGHT) {
            if (mess_debug) {
                System.out.println("Server says: Bot #" + bot_id + " should move right " + instruction);
            }
        }

        if (instruction == Graph.LEFT) {
            if (mess_debug) {
                System.out.println("Server says: Bot #" + bot_id + " should move left " + instruction);
            }
        }
        if (instruction == Graph.STRAIGHT) {
            if (mess_debug) {
                System.out.println("Server says: Bot #" + bot_id + " should move straight " + instruction);
            }
        }
        if (instruction == Graph.BACKWARD) {
            if (mess_debug) {
                System.out.println("Server says: Bot #" + bot_id + " should move backward " + instruction);
            }
        }
    }

    
    public static void main(String[] args) {
        CommunicationAPI capi = new CommunicationAPI("/dev/ttyUSB0");
        capi.open();
        //initially patient sends message that I want water
        //Keep sending acks
        //Boolean if_send = true;
        Character patient_mess = (char) 5 * 32 + RequestHandler.WATER;
        capi.send(Character.toString(patient_mess));
        //int ack = Bot.bot2 * 32 + RequestHandler.ACK;
        //char ack_message = (char) ack;
        //capi.send(Character.toString(ack_message));
        //int inprogress = Bot.bot2 * 32 + RequestHandler.IN_PROGRESS;
        //char inprogress_message = (char) inprogress;
       /* while (true) {
            Character inp_mess = null;
            if (DEBUG) {
                System.out.println(" getting next_char_in_buffer");
            }
            inp_mess = capi.next_char_in_buffer();

            if (Bot.get_id_mess(inp_mess) == Bot.bot2) {
                print_mess(inp_mess);

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
        }*/
    }
}
