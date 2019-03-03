import gnu.io.*;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;

public class Communicator implements SerialPortEventListener {

    hoomanCallback callback = null;

    //for containing the ports that will be found
    public Enumeration ports = null;
    //map the port names to CommPortIdentifiers
    public HashMap portMap = new HashMap();

    //this is the object that contains the opened port
    public CommPortIdentifier selectedPortIdentifier = null;
    public SerialPort serialPort = null;

    //input and output streams for sending and receiving data
    public InputStream input = null;
    public OutputStream output = null;

    //just a boolean flag that i use for enabling
    //and disabling buttons depending on whether the program
    //is connected to a serial port or not
    public boolean bConnected = false;

    //the timeout value for connecting with the port
    final static int TIMEOUT = 2000;
    
    public void searchForPorts() {
        ports = CommPortIdentifier.getPortIdentifiers();

        while (ports.hasMoreElements()) {
            CommPortIdentifier curPort = (CommPortIdentifier) ports.nextElement();

            //get only serial ports
            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
//                window.cboxPorts.addItem(curPort.getName());
                portMap.put(curPort.getName(), curPort);
            }
        }
    }

    public Communicator(hoomanCallback callback) {
        this.callback = callback;
    }

    public Communicator() {
    }

    final public boolean getConnected() {
        return bConnected;
    }

    public void setConnected(boolean bConnected) {
        this.bConnected = bConnected;
    }

    // stop bit: 0 -> 1, 1 -> 2, 2 -> 1.5
    // parity: 0 -> none, 1 -> odd, 2-> even, 3 -> mark, 4 -> space
    public boolean connect(String name, int buadrate, int databits, int stopbit, int parity) {
        selectedPortIdentifier = (CommPortIdentifier) portMap.get(name);

        CommPort commPort = null;

        try {
            //the method below returns an object of type CommPort
            commPort = selectedPortIdentifier.open("TigerControlPanel", TIMEOUT);
            //the CommPort object can be casted to a SerialPort object
            serialPort = (SerialPort) commPort;

            //for controlling GUI elements
            setConnected(true);

            serialPort.setSerialPortParams(buadrate, databits, stopbit, parity);
            return true;
        } catch (PortInUseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean initIOStream() {
        //return value for whather opening the streams is successful or not
        boolean successful = false;

        try {
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
            successful = true;
            return successful;
        } catch (IOException e) {
            return successful;
        }
    }

    public void initListener() {
        try {
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (TooManyListenersException e) {
        }
    }

    public void disconnect() {
        //close the serial port
        try {
            serialPort.removeEventListener();
            serialPort.close();
            input.close();
            output.close();
            setConnected(false);
            // window.keybindingController.toggleControls();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void serialEvent(SerialPortEvent evt) {
        if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                byte singleData = (byte) input.read();
                callback.processData(new String(new byte[] {singleData}));
            } catch (Exception e) {

            }
        }
    }

    public void writeByte(byte[] byteToWrite) {
        try {
            output.write(byteToWrite);
            output.flush();
        } catch (Exception e) {

        }
    }

    public void writeString(String stringToWrite) {
        byte[] bytetoWrite = stringToWrite.getBytes();
        writeByte(bytetoWrite);
    }
}
