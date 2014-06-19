/*
 * =====================================================================================
 *
 *       Filename:  SimpleRead.java
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


public class SimpleRead implements Runnable, SerialPortEventListener {

   static CommPortIdentifier portId;
   static Enumeration portList;
   InputStream inputStream;
   SerialPort serialPort;
   Thread readThread;
   

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
               System.out.println("Found port: " + defaultPort);
               portFound = true;
               SimpleRead reader = new SimpleRead();
               System.out.println("Creating new SimpleRead");
            }
         }
      }
      if (!portFound) {
         System.out.println("port " + defaultPort + " not found.");
      }

   }

   public SimpleRead() {
      try {
         serialPort = (SerialPort) portId.open("SimpleReadApp", 20000);
      } catch (PortInUseException e) {
      }
      try {
         inputStream = serialPort.getInputStream();
      } catch (IOException e) {
      }

      try {
         serialPort.addEventListener(this);
      } catch (TooManyListenersException e) {
      }

      serialPort.notifyOnDataAvailable(true);

      try {
         serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8,
                 SerialPort.STOPBITS_1,
                 SerialPort.PARITY_NONE);
      } catch (UnsupportedCommOperationException e) {
      }

      readThread = new Thread(this);
      readThread.start();
   }

   public void run() {
      try {
         while (true) {
            Thread.sleep(2000);
         }
      } catch (InterruptedException e) {
      }
   }

   public void serialEvent(SerialPortEvent event) {
      switch (event.getEventType()) {

         case SerialPortEvent.BI:

         case SerialPortEvent.OE:

         case SerialPortEvent.FE:

         case SerialPortEvent.PE:

         case SerialPortEvent.CD:

         case SerialPortEvent.CTS:

         case SerialPortEvent.DSR:

         case SerialPortEvent.RI:

         case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
            break;

         case SerialPortEvent.DATA_AVAILABLE:
            List<Character> lc = new ArrayList<Character>();
            try {
               while (inputStream.available() > 0) {
                  int numBytes = inputStream.read();
                  if (((char) numBytes) != '\n') {
                     lc.add((char) numBytes);
                  }
               }
               for (int i = 0; i < lc.size(); i++) {
                  System.out.print(lc.get(i));
               }
               //System.out.println("");
               System.out.flush();
               //System.out.print(new String(readBuffer));
            } catch (IOException e) {
            }

            break;
      }
   }
}
