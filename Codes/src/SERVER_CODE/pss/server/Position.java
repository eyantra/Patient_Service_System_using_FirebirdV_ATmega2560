/*
 * =====================================================================================
 *
 *       Filename:  Position.java
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

/**This class encodes the information regarding the position of the bot<br/>
 * present     : Last node on which the bot was present<br/>
 * orientation : it is facing NORTH, EAST,SOUTH, WEST<br/>
 */
public class Position {

    /**
     *  Location (Integer as seen on the Arena map)
     */
    public int present;
    /**
     * Orientation (NORTH, SOUTH, EAST or WEST) encoded as an integer
     */
    public int orientation;

    /**
     * Constructor
     * @param present Location (Integer as seen on the Arena map)
     * @param orientation Orientation (NORTH, SOUTH, EAST or WEST) encoded as an integer
     * @see Graph
     */
    public Position(int present, int orientation) {
        this.present = present;
        this.orientation = orientation;
    }

    /**
     * Default constructor
     */
    public Position(){
        
    }
}
