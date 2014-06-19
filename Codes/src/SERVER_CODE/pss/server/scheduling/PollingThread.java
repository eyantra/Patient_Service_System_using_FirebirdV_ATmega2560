/*
 * =====================================================================================
 *
 *       Filename:  PollingThread.java
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

package pss.server.scheduling;

import java.io.IOException;
import pss.configuration.Configure;
import pss.serialcomm.CommunicationAPI;
import pss.server.Bot;
import pss.server.RequestHandler;

/**
 * This thread cyclically polls all the entities in the system (bots/ patients) asking about their status and
 * returning their current status which the provide over Zigbee encoded in an 8 bit character
 */
public class PollingThread {

    /**
     *Communication API Object
     * @see CommunicationAPI
     */
    CommunicationAPI capi;
    /**
     * ID of the bot to be polled in the next loop
     */
    public int poll_id = Bot.bot1;
    /**
     * Number of serving bots
     */
    public int number_bots = 0;
    /**
     * Flag indicating that in the next turn patient must be polled
     */
    public static final int poll_patient = 0;
    /**
     *Flag indicating that in the next turn bot must be polled
     */
    public static final int poll_bot = 1;

    /**
     * Indicates who (patient or bot) must be polled in the next turn
     */
    public static int bot_or_patient = poll_bot;
    /**
     * Debugging flag
     */
    public static final Boolean DEBUG = false;
    /**
     * 8 bit encoding of "POLL" message for a bot
     */
    public static int BOT_POLLING = 5;
    /**
     * 8 bit encoding of "POLL" message for a patient (0x80)
     */
    public static int PATIENT_POLLING = 128;
    /**
     * Request Handler Object
     * @see RequestHandler
     */
    private RequestHandler rh;

    /**
     * Constructor
     * @param capi Communication API
     * @see CommunicationAPI
     */
    public PollingThread(CommunicationAPI capi) {
        this.capi = capi;
        bot_or_patient = poll_bot;
        try {
            Configure.getInstance();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        number_bots = Configure.NUM_BOTS;
    }

    /**
     * Poll the next entity bot or patient
     * @param poll_bot_or_patient
     * @return reponse from the entity encoded as a character
     */
    public Character poll_next(int poll_bot_or_patient) {
        while (true) {
            if (poll_bot_or_patient == poll_bot) {
                if (DEBUG) {
                    System.out.println("polling bot " + poll_id);
                }
                int poll_message = poll_id * 32 + BOT_POLLING;
                capi.send(Character.toString((char) poll_message));
                Character c = capi.next_char_in_buffer();
                if (c == 0xff) {
                    continue;
                }
                poll_id = (poll_id + 1) % number_bots;
                return c;
            } else {
                int poll_message = PATIENT_POLLING;
                capi.send(Character.toString((char) poll_message));
                Character c = capi.next_char_in_buffer();
                if (c == 0xff) {
                    continue;
                }
                return c;
            }
        }
    }
}
