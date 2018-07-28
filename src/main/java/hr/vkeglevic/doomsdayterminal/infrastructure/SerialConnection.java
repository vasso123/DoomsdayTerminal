package hr.vkeglevic.doomsdayterminal.infrastructure;

import hr.vkeglevic.doomsdayterminal.model.Connection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import jssc.SerialPort;
import jssc.SerialPortException;

/**
 *
 * @author vanja
 */
public class SerialConnection implements Connection {

    private final String portName;
    private final int baudRate;
    private final int dataBits;
    private final int stopBits;
    private final int parity;
    private SerialPort port;
    private InputStream inputStream;
    private OutputStream outputStream;
    private final int flowControl;

    public SerialConnection(
            String portName,
            int baudRate,
            int dataBits,
            int stopBits,
            int parity,
            int flowControl
    ) {
        this.portName = portName;
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;
        this.flowControl = flowControl;
    }

    @Override
    public void open() throws IOException {
        try {
            port = new SerialPort(portName);
            port.openPort();
            port.setParams(baudRate, dataBits, stopBits, parity);
            port.setFlowControlMode(flowControl);
        } catch (SerialPortException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        if (port != null && port.isOpened()) {
            try {
                port.closePort();
            } catch (SerialPortException ex) {
                throw new IOException(ex);
            }
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (inputStream == null) {
            inputStream = new SerialPortInputStream(port);
        }
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            outputStream = new SerialPortOutputStream(port);
        }
        return outputStream;
    }

    @Override
    public String toString() {
        return "SerialConnection{" + "portName=" + portName + ", baudRate=" + baudRate + ", dataBits=" + dataBits + ", stopBits=" + stopBits + ", parity=" + parity + ", port=" + port + ", flowControl=" + flowControl + '}';
    }

}
