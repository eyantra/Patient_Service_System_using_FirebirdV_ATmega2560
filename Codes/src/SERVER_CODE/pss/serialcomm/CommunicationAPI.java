/*
 * =====================================================================================
 *
 *       Filename:  CommunicationAPI.java
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

package pss.serialcomm;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @since 22 Mar
 * @depends RXTXComm
 * @author rohit
 */
/**
	Provides a multithreaded API for accessing serial port
*/
public class CommunicationAPI {

    static CommPortIdentifier portId;
    static Enumeration portList;
    InputStream inputStream;
    SerialPort serialPort;
    String defaultPort;
    Thread readThread;
    static OutputStream outputStream;
    static boolean outputBufferEmptyFlag = false;
    static ConcurrentLinkedQueue<Character> in_buffer = new ConcurrentLinkedQueue<Character>();
    public static final Boolean DEBUG = false;

    public CommunicationAPI(String port) {
        this.defaultPort = port;

    }

		/**
			Close the serial port
		*/
    public void close() {
        serialPort.close();
    }

		/*
			Open the serial port for 2-way communication
		*/
    public void open() {
        boolean portFound = false;
        portList = CommPortIdentifier.getPortIdentifiers();

        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (portId.getName().equals(defaultPort)) {
                    if (DEBUG) {
                        System.out.println("Found port: " + defaultPort);
                    }
                    portFound = true;
                    try {
                        serialPort = (SerialPort) portId.open("communication-api", 2000000);
                    } catch (PortInUseException e) {
                        if (DEBUG) {
                            System.out.println("Please connect X-bee properly!");
                        }
                    }
                    try {
                        inputStream = serialPort.getInputStream();
                    } catch (IOException e) {
                    }
                    try {
                        outputStream = serialPort.getOutputStream();
                    } catch (IOException e) {
                    }
                    Receiver x = new Receiver();
                    try {
                        serialPort.addEventListener(x);
                    } catch (TooManyListenersException e) {
                    }
                    serialPort.notifyOnDataAvailable(true);
                    try {
                        serialPort.setSerialPortParams(9600,
                                SerialPort.DATABITS_8,
                                SerialPort.STOPBITS_1,
                                SerialPort.PARITY_NONE);
                    } catch (UnsupportedCommOperationException e) {
                    }
                    readThread = new Thread(x);
                    readThread.start();
                }
            }
        }
        if (!portFound) {
            if (DEBUG) {
                System.out.println("port " + defaultPort + " not found.");
            }
        }
    }

	/**
		To send a message, it creates a new thread which waits for the message to be sent.
		This has been done for maximum concurrency
	*/
    class Sender implements Runnable {

        String messageString;

        public Sender(String messString) {
            this.messageString = messString;
        }

        public void run() {
				try{
		         try {
		             System.out.println("Sending \"" + (int) messageString.charAt(0) + "\" to " + serialPort.getName());
		             outputStream.write((String.valueOf(messageString)).charAt(0));
		             outputStream.flush();
		         } catch (IOException e) {
		         }
		         try {
		             Thread.sleep(2000);  // Be sure data is xferred before closing
		         } catch (Exception e) {
		         }
				}catch(Exception e){
					System.out.println("Zigbee not found\n Please check the configuration file");				
					System.exit(1);
				}
        }
    }

    public void send(String messageString) {
        Thread s = new Thread(new Sender(messageString));
        s.start();
    }

	/**
		A receiver thread is always on!
	*/
    public void receive() {
        Receiver x = new Receiver();
        readThread = new Thread(x);
        readThread.start();
    }

    public void getNextCharFromBufferIfPresent() {
        in_buffer.poll();
    }

    public Character next_char_in_buffer() {
        Character x = null;
        long time1 = System.currentTimeMillis();
        long time2 = 0;
        while (true) {
            if (x != null) {
                System.out.println("received message " + (int) x);
                return x;
            }
            x = in_buffer.poll();
            time2 = System.currentTimeMillis();
            if(time2 - time1 > 2000){
               System.out.println("Timeout!");
               x = 0xff;
               return x;
            }
        }
    }

	/**
		Implementation of the thread which receives. Interrupt Based to provide maximum concurrency.
	*/
    class Receiver implements Runnable, SerialPortEventListener {

        public Receiver() {
        }

        public void run() {
            try {
                while (true) {
                    Thread.sleep(20000);
                }
            } catch (InterruptedException e) {
            }
        }

        public void serialEvent(SerialPortEvent event) {
            switch (event.getEventType()) {
                /** Other events are not interesting.*/
                case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                    break;

                case SerialPortEvent.DATA_AVAILABLE:
                    try {
                        while (inputStream.available() > 0) {
                            int char_bytes = inputStream.read();
                            in_buffer.add((char) char_bytes);
                            if (DEBUG) {
                                System.out.println("RECIEVED : " + (int) char_bytes);
                            }
                        }
                    } catch (IOException e) {
                    }
                    break;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        CommunicationAPI capi = new CommunicationAPI("/dev/ttyUSB1");
        capi.open();
        capi.send(Character.toString(((char) 160)));
        capi.next_char_in_buffer();

        Thread.sleep(1);
    }

    ;
}
