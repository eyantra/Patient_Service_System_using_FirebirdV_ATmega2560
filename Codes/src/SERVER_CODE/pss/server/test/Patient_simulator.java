/*
 * =====================================================================================
 *
 *       Filename:  Patient_simulator.java
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
import pss.server.RequestHandler;

/**
 * Simulates a patient sending requests via zigbee to the main server
 *
 */
public class Patient_simulator {

    public static void main(String[] args) {
        CommunicationAPI capi = new CommunicationAPI("/dev/ttyUSB1");
        capi.open();
        Character patient_mess = (char) 6 * 32 + RequestHandler.WATER;
        capi.send(Character.toString(patient_mess));
    }
}
