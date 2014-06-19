/*
 * =====================================================================================
 *
 *       Filename:  GraphTester.java
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

import pss.server.Graph;
import pss.server.Position;

/**
 * Module to test correctness of pathfinding algorithm on graph.
 * Specify an initial position and the destination patient id (Hardcoded in main).
 * Prints out the sequence of action bot must take to follow the computed path.
 */
public class GraphTester {

    public static void main(String[] args) {
        Graph g =new Graph();
        g.init_graph();
        Position curr = new Position();
        curr.present =0;
        curr.orientation=Graph.WEST;
        int patient_id=-2;
        g.move_the_bot(curr,patient_id);
    }
}
