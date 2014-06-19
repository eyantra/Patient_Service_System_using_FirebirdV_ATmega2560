/*
 * =====================================================================================
 *
 *       Filename:  Utils.java
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


/**
 * Actual encoding of messages for each basic instruction LEFT,RIGHT,FORWARD and BACKWARD  along with ID of bot which should perform this 
 * action into an into 8 bit number
 * @author rohit
 */
public class Utils {

    /**
     * Gives the char representing the instruction to tell bot with id bot_is to turn left
     * @param bot_id
     * @return char encoding of the instruction
     * @see Bot
     */
    public static char left_message(int bot_id) {
        int message = bot_id * 32 + Graph.LEFT;
        return ((char) message);
    }

    /**
     * Gives the char representing the instruction to tell bot with id bot_is to turn right 
     * @param bot_id
     * @return char encoding of the instruction
     * @see Bot
     */
    public static char right_message(int bot_id) {
        int message = bot_id * 32 + Graph.RIGHT;
        return ((char) message);
    }

    /**
     * Gives the char representing the instruction to tell bot with id bot_is to move straight
     * @param bot_id
     * @return char encoding of the instruction
     * @see Bot
     */
    public static char straight_message(int bot_id) {
        int message = bot_id * 32 + Graph.STRAIGHT;
        return ((char) message);
    }

    /**
     * Gives the char representing the instruction to tell bot with id bot_is to turn backward 180 degrees
     * @param bot_id
     * @return char encoding of the instruction
     * @see Bot
     */
    public static char back_message(int bot_id) {
        int message = bot_id * 32 + Graph.BACKWARD;
        System.out.println((char) message);
        return ((char) message);
    }
}
