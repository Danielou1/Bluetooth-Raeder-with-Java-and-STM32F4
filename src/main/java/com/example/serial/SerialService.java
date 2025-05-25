package com.example.serial;

import com.fazecast.jSerialComm.SerialPort;

public class SerialService {
    private SerialPort serialPort;

    public SerialPort[] getAvailablePorts() {
        return SerialPort.getCommPorts();
    }

    public boolean openPort(String portName, int baudRate) {
        for (SerialPort port : getAvailablePorts()) {
            if (port.getSystemPortName().equalsIgnoreCase(portName)) {
                port.setBaudRate(baudRate);
                if (port.openPort()) {
                    serialPort = port;
                    return true;
                }
            }
        }
        return false;
    }

    public void closePort() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
        }
    }

    public String readData() {
        try {
            if (serialPort.bytesAvailable() > 0) {
                byte[] buffer = new byte[serialPort.bytesAvailable()];
                serialPort.readBytes(buffer, buffer.length);
                return new String(buffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean isPortOpen() {
        return serialPort != null && serialPort.isOpen();
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }
}
