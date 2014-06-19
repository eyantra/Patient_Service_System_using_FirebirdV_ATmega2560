/*
 * =====================================================================================
 *
 *       Filename:  Graph.java
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

import java.util.HashMap;
import java.util.Map;

/**
 * <p>This class contains all information about the floor plan of the hospital. In the present implementation the arena has been
 * hardcoded in this class. If the floor plan or bot routes are modified then this is the only class that needs to be changed.
 * All distance orientation and path finding functions are part of this graph. Currently the arena is as follows.</p>
 * <img src="Arena.png"/>
 */
public class Graph {

    /**
     * store the node location (room no.) of patients according to patient id.
     */
    static Map<Integer, Integer> patientPos = new HashMap<Integer, Integer>();
    /**
     *first co-ordinate denotes the position of the bot.<br/>
     *Second denote the orientation<br/>
     *  0 = right,1=left,2=up,3=down<br/>
     *third index denotes the diretcion in which bot will turn.<br/>
     *  0=right,1=left,2=straight,3=backward.<br/>
     *  (A straight turn means no turn)<br/>
     *The statement<br/>
     *  grph[a][b][c] = d<br/>
     *is to be interpreted as follows.<br/>
     *  at "a" facing "b", if you turn "c" you will be facing "d"<br/>
     */
    public int[][][] grph = new int[16][4][4];
    public static final int RIGHT = 0, LEFT = 1, STRAIGHT = 2, BACKWARD = 3, FINISH = 5, NOPATH = -2, BOT_POLLING = 5, PATIENT_POLLING = 6;     // R=right, L=left,F=forward,B=backward. it indicates the direction in which you move.
    public static final int NORTH = 0, SOUTH = 1, EAST = 2, WEST = 3;
    /*
     * Place holder for a distance that cannot be acheived on any shortest path in the graph and hence can be safely
     * treated as infinity.
     */
    public static final int INFINTY = 25;
    /**
     * Array used for storing the memoization results of the distance calculation algorithm <br/>
     *The statement <br/>
     *  distance_pos_ori_pos[a][b][c] = d <br/>
     *is to be interpreted as follows. <br/>
     *  starting at "a" facing "b"; in order to reach "c" you need to make "d" moves. <br/>
     */
    public int[][][] distance_pos_ori_pos = new int[16][4][16];
    /**
     * Debugging flags.
     */
    boolean DEBUG = false;
    /**
     * Debugging variable for number of recursive calls of distance computing function
     */
    int no_calls = 0;

    /**
     *initialize the distance from one position to another position to infinity <br/>
     *initialize the position of the patients <br/>
     *initialize graph and set the respective neighbours <br/>
     */
    public void init_graph() {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 16; k++) {
                    distance_pos_ori_pos[i][j][k] = INFINTY;
                }
            }
        }

        patientPos.put(-1, 0);
        patientPos.put(2, 5);
        patientPos.put(3, 9);
        patientPos.put(4, 13);
        patientPos.put(5, 10);
        patientPos.put(6, 6);
        patientPos.put(7, 1);
        patientPos.put(-2, 14);
        patientPos.put(-3, 15);

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    grph[i][j][k] = -1;
                }
            }
        }
        grph[0][WEST][RIGHT] = 15;
        grph[0][WEST][LEFT] = 14;
        grph[0][NORTH][STRAIGHT] = 15;
        grph[0][NORTH][RIGHT] = 3;
        grph[0][SOUTH][STRAIGHT] = 14;
        grph[0][SOUTH][LEFT] = 3;
        grph[0][EAST][STRAIGHT] = 3;

        grph[15][NORTH][BACKWARD] = 0;
        grph[15][SOUTH][STRAIGHT] = 0;
        grph[14][SOUTH][BACKWARD] = 0;
        grph[14][NORTH][STRAIGHT] = 0;

        grph[3][EAST][LEFT] = 4;
        grph[3][NORTH][LEFT] = 0;
        grph[3][WEST][STRAIGHT] = 0;
        grph[3][NORTH][STRAIGHT] = 4;

        grph[4][EAST][STRAIGHT] = 8;
        grph[4][EAST][LEFT] = 5;
        grph[4][SOUTH][LEFT] = 8;
        grph[4][NORTH][STRAIGHT] = 5;
        grph[4][NORTH][RIGHT] = 8;

        grph[8][EAST][STRAIGHT] = 12;
        grph[8][EAST][LEFT] = 9;
        grph[8][SOUTH][LEFT] = 12;
        grph[8][NORTH][STRAIGHT] = 9;
        grph[8][NORTH][RIGHT] = 12;

        grph[12][EAST][LEFT] = 13;
        grph[12][EAST][RIGHT] = 11;
        grph[12][SOUTH][STRAIGHT] = 11;
        grph[12][NORTH][STRAIGHT] = 13;
        grph[12][NORTH][BACKWARD] = 11;

        grph[11][SOUTH][STRAIGHT] = 10;
        grph[11][SOUTH][RIGHT] = 7;
        grph[11][NORTH][LEFT] = 7;
        grph[11][WEST][STRAIGHT] = 7;

        grph[7][SOUTH][STRAIGHT] = 6;
        grph[7][SOUTH][RIGHT] = 2;
        grph[7][NORTH][LEFT] = 2;
        grph[7][WEST][STRAIGHT] = 2;
        grph[7][WEST][LEFT] = 6;

        grph[2][SOUTH][STRAIGHT] = 1;
        grph[2][SOUTH][BACKWARD] = 3;
        grph[2][NORTH][STRAIGHT] = 3;
        grph[2][WEST][LEFT] = 1;
        grph[2][WEST][RIGHT] = 3;

        grph[5][NORTH][BACKWARD] = 4;
        grph[5][SOUTH][STRAIGHT] = 4;

        grph[9][NORTH][BACKWARD] = 8;
        grph[9][SOUTH][STRAIGHT] = 8;

        grph[13][NORTH][BACKWARD] = 12;
        grph[13][SOUTH][STRAIGHT] = 12;

        grph[10][NORTH][STRAIGHT] = 11;
        grph[10][SOUTH][BACKWARD] = 11;

        grph[6][NORTH][STRAIGHT] = 7;
        grph[6][SOUTH][BACKWARD] = 7;

        grph[1][NORTH][STRAIGHT] = 2;
        grph[1][SOUTH][BACKWARD] = 2;
    }

    /**THis function calculate the new position after a left turn.
     * @param p Current Position
     * @return  the new Postion after a left turn
     * @see Position
     */
    public Position Left_turn(Position p) {
        Position t = new Position();
        t.present = p.present;
        switch (p.orientation) {
            case WEST:
                t.orientation = SOUTH;
                break;
            case EAST:
                t.orientation = NORTH;
                break;
            case NORTH:
                t.orientation = WEST;
                break;
            case SOUTH:
                t.orientation = EAST;
                break;
        } //send command to bot.
        return t;
    }

    /**This function computes the new position after a 180 degree turn
     * @param p Current Position
     * @return  the new Postion after a 180 degree turn
     * @see Position
     */
    public Position Back(Position p) {
        Position t = new Position();
        t.present = p.present;
        switch (p.orientation) {
            case WEST:
                t.orientation = EAST;
                break;
            case EAST:
                t.orientation = WEST;
                break;
            case NORTH:
                t.orientation = SOUTH;
                break;
            case SOUTH:
                t.orientation = NORTH;
                break;
        }
        return t;
    }

    /**This function computes the new position after a right turn.
     * @param p Current Position
     * @return  the new Postion after a right turn
     * @see Position
     */
    public Position Right_turn(Position p) {
        Position t = new Position();
        t.present = p.present;
        switch (p.orientation) {
            case WEST:
                t.orientation = NORTH;
                break;
            case EAST:
                t.orientation = SOUTH;
                break;
            case NORTH:
                t.orientation = EAST;
                break;
            case SOUTH:
                t.orientation = WEST;
                break;
        }
        return t;
    }

    /**This function computes the new position if no turn is taken and the bot just moves straight upto the next cross.
     * @param p Current Position
     * @return  the new Postion after moving straight in the direction it is facing
     * @see Position
     */
    public Position Straight(Position p) {
        Position t = new Position();
        t.present = grph[p.present][p.orientation][STRAIGHT];
        t.orientation = p.orientation;
        return t;
    }

    /**Compute the new Position of the bot after a specific action FORWARD, LEFT_TURN, RIGHT_TURN or BACKWRD (encoded as an int)
     * @param curr Current position
     * @param action Next action
     * @return New postion
     * @see Position
     */
    public Position pos_after_action(Position curr, int action) {
        if (action == LEFT) {
            if (DEBUG) {
                System.out.println("Turn left");
            }
            return Left_turn(curr);
        } else if (action == RIGHT) {
            if (DEBUG) {
                System.out.println(" Turn right");
            }
            return Right_turn(curr);
        }
        if (action == STRAIGHT) {
            if (DEBUG) {
                System.out.println("Move Straight");
            }
            return Straight(curr);
        }
        if (action == BACKWARD) {
            if (DEBUG) {
                System.out.println("Reverse");
            }
            return Back(curr);
        } else {
            System.exit(0);
            return null;
        }
    }

    /**
     * Tester function to be used to print the path in the Graph Tester module
     * @param curr          Current bot position
     * @param patient_id    Destination patient id
     * @see Patient
     */
    public void move_the_bot(Position curr, int patient_id) {
        boolean success = true;

        while (curr.present != patientPos.get(patient_id)) {
            System.out.println("Position is " + curr.present + " orientation is  " + curr.orientation);
            int action = search(patient_id, curr);
            curr = pos_after_action(curr, action);
            if (curr == null) {
                success = false;
                break;
            }
        }
        if (success == false) {
            System.out.println("path does not exist");
        }
    }

    /**
     * Recurrsive function to compute distance (in terms of number of edges left to traverse), Memoization used for optimization
     * @param curr      Current position
     * @param final_pos Final position
     * @param counter   Countdown to avoid getting stuck in cycles during recursive calls. Initialized to "infinity"
     * @return          Distance to the destination
     * @see Position
     */
    public int find_distance(Position curr, int final_pos, int counter) {
        int min_distance = INFINTY;
        no_calls++;
        if (DEBUG) {
            System.out.println("Number of calls is " + no_calls + "counter is " + counter);
            System.out.println("Position is : " + curr.present + " Orientation : " + curr.orientation + " Counter : " + counter);
        }
        if (distance_pos_ori_pos[curr.present][curr.orientation][final_pos] < INFINTY) {
            return distance_pos_ori_pos[curr.present][curr.orientation][final_pos];
        }
        if (curr.present == final_pos) {
            return 0;
        }
        if (counter == 0) {
            return INFINTY + 1;
        }
        if (DEBUG) {
            System.out.println("Number of calls is " + no_calls + "counter is " + counter);
        }
        if (grph[curr.present][curr.orientation][LEFT] != -1) {
            if (DEBUG) {
                System.out.println("Searching in left for position : " + curr.present + " orientation " + curr.orientation);
                System.out.println(" " + Left_turn(curr).present + " orientation " + Left_turn(curr).orientation);
            }
            int d1 = find_distance(Left_turn(curr), final_pos, counter - 1);
            if (d1 < min_distance) {
                min_distance = d1;
            }
        }
        if (grph[curr.present][curr.orientation][RIGHT] != -1) {
            if (DEBUG) {
                System.out.println("Searching in right for position : " + curr.present + " orientation " + curr.orientation);
                System.out.println(" " + Right_turn(curr).present + " orientation " + Right_turn(curr).orientation);
            }
            int d1 = find_distance(Right_turn(curr), final_pos, counter - 1);
            if (d1 < min_distance) {
                min_distance = d1;
            }
        }
        if (grph[curr.present][curr.orientation][STRAIGHT] != -1) {
            if (DEBUG) {
                System.out.println("Searching in forward for position : " + curr.present + " orientation " + curr.orientation);
            }
            int d1 = find_distance(Straight(curr), final_pos, counter - 1);
            if (d1 < min_distance) {
                min_distance = d1;
            }
        }
        if (grph[curr.present][curr.orientation][BACKWARD] != -1) {
            if (DEBUG) {
                System.out.println("Searching in backward for position : " + curr.present + " orientation " + curr.orientation);
            }
            int d1 = find_distance(Back(curr), final_pos, counter - 1);
            if (d1 < min_distance) {
                min_distance = d1;
            }
        }
        if (min_distance == INFINTY) {
            if (DEBUG) {
                System.out.println("MAX_DISTANCE No path: " + curr.present + " orientation " + curr.orientation);
            }
            distance_pos_ori_pos[curr.present][curr.orientation][final_pos] = INFINTY;
            return INFINTY;
        } else {
            distance_pos_ori_pos[curr.present][curr.orientation][final_pos] = min_distance + 1;
            return min_distance + 1;
        }

    }

    /**
     * Default Constructor
     */
    public Graph() {
        this.init_graph();
    }

    /**
     * Initial location of bot 1.
     */
    public static final int SERVER_ID1_position = 14;
    /**
     * Initial location of bot 2.
     */
    public static final int SERVER_ID2_position = 15;
    /**
     * Initial orientation of bot 1.
     */
    public static final int SERVER_ID1_orientation = Graph.SOUTH;
    /**
     * Initial orientation of bot 1.
     */
    public static final int SERVER_ID2_orientation = Graph.NORTH;

    /**
     * ID of server location for bot 1
     */
    public static final int SERVER_ID1 = -2;
    /**
     * ID of server location for bot 2
     */
    public static final int SERVER_ID2 = -3;

    /**Seraches the graph and find out the action to be taken on the path to reach the patient.
     * @param patient_id    Destination patient id
     * @param curr          Current position
     * @return              Next action to be taken on the path to reach the patient.
     * @see Position
     */
    public int search(int patient_id, Position curr) {
        int final_pos = patientPos.get(patient_id);
        /**@TODO check if same as well*/
        int min_distance = INFINTY;
        int action = 13;
        int max_counter = INFINTY;
        if (final_pos == curr.present) {
            return FINISH;
        }
        if (grph[curr.present][curr.orientation][LEFT] != -1) {
            if (DEBUG) {
                System.out.println("Searching in left for position : " + curr.present + " orientation " + curr.orientation);
                System.out.println("Sending in left position : " + Left_turn(curr).present + " orientation " + Left_turn(curr).orientation);
            }
            int d1 = find_distance(Left_turn(curr), final_pos, max_counter);
            if (d1 < min_distance) {
                action = LEFT;
                min_distance = d1;
            }
        }
        if (grph[curr.present][curr.orientation][RIGHT] != -1) {
            if (DEBUG) {
                System.out.println("Searching in right for position : " + curr.present + " orientation " + curr.orientation);
            }
            int d1 = find_distance(Right_turn(curr), final_pos, max_counter);
            if (d1 < min_distance) {
                action = RIGHT;
                min_distance = d1;
            }
        }
        if (grph[curr.present][curr.orientation][STRAIGHT] != -1) {
            if (DEBUG) {
                System.out.println("Searching in forward for position : " + curr.present + " orientation " + curr.orientation);
            }
            int d1 = find_distance(Straight(curr), final_pos, max_counter);
            if (d1 < min_distance) {
                action = STRAIGHT;
                min_distance = d1;
            }
        }
        if (grph[curr.present][curr.orientation][BACKWARD] != -1) {
            if (DEBUG) {
                System.out.println("Searching in backward for position : " + curr.present + " orientation " + curr.orientation);
            }
            int d1 = find_distance(Back(curr), final_pos, max_counter);
            if (d1 < min_distance) {
                action = BACKWARD;
                min_distance = d1;
            }
        }
        if (min_distance == INFINTY) {
            if (DEBUG) {
                System.out.println("NOPATH : " + curr.present + " orientation " + curr.orientation);
            }
            return NOPATH;
        } else {
            if (DEBUG) {
                System.out.println("minimum distance is " + min_distance);
            }
            return action;
        }
    }
}
