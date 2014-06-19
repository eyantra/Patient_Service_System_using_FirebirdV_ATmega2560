/*
 * =====================================================================================
 *
 *       Filename:  SimpleWrite.java
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
 *        Company:  Oracle
 *		  Copyright:  Oracle
 *
 * =====================================================================================
 */

package pss.server.test;

import java.io.*;
import java.util.*;
import gnu.io.*;

public class SimpleWrite {

   static Enumeration portList;
   static CommPortIdentifier portId;
   static String messageString = "rohit kumar saraf";
   static SerialPort serialPort;
   static OutputStream outputStream;
   static boolean outputBufferEmptyFlag = false;

   public static void main(String[] args) {
      boolean portFound = false;
      String defaultPort = "/dev/ttyUSB1";

      if (args.length > 0) {
         defaultPort = args[0];
      }

      portList = CommPortIdentifier.getPortIdentifiers();

      while (portList.hasMoreElements()) {
         portId = (CommPortIdentifier) portList.nextElement();

         if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {

            if (portId.getName().equals(defaultPort)) {
               System.out.println("Found port " + defaultPort);

               portFound = true;

               try {
                  serialPort = (SerialPort) portId.open("SimpleWrite", 2000);
               } catch (PortInUseException e) {
                  System.out.println("Port in use.");

                  continue;
               }

               try {
                  outputStream = serialPort.getOutputStream();
               } catch (IOException e) {
               }

               try {
                  serialPort.setSerialPortParams(9600,
                          SerialPort.DATABITS_8,
                          SerialPort.STOPBITS_1,
                          SerialPort.PARITY_NONE);
               } catch (UnsupportedCommOperationException e) {
               }


               try {
                  serialPort.notifyOnOutputEmpty(true);
               } catch (Exception e) {

                  System.out.println("Error setting event notification");
                  System.out.println(e.toString());
                  System.exit(-1);
               }
               System.out.println(
                       "Writing \"" + messageString + "\" to "
                       + serialPort.getName());
               try {
                  outputStream.write((String.valueOf(messageString)).getBytes());
                  outputStream.flush();
               } catch (IOException e) {
               }
               try {
                  Thread.sleep(2000);  // Be sure data is xferred before closing
               } catch (Exception e) {
               }
               serialPort.close();
            }
         }
      }
      if (!portFound) {
         System.out.println("port " + defaultPort + " not found.");
      }
   }
}
