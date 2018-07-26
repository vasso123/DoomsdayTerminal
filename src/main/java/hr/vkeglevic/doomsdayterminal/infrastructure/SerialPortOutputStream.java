package hr.vkeglevic.doomsdayterminal.infrastructure;

import java.io.IOException;
import java.io.OutputStream;
import jssc.SerialPort;
import jssc.SerialPortException;

/**
 *
 * @author vanja
 */
public class SerialPortOutputStream extends OutputStream {

    private final SerialPort port;

    public SerialPortOutputStream(SerialPort port) {
        this.port = port;
    }

    @Override
    public void write(int b) throws IOException {
        try {
            port.writeByte((byte) b);
        } catch (SerialPortException ex) {
            throw new IOException(ex);
        }
    }

}
