package hr.vkeglevic.doomsdayterminal.infrastructure;

import java.io.IOException;
import java.io.InputStream;
import jssc.SerialPort;
import jssc.SerialPortException;

/**
 *
 * @author vanja
 */
public class SerialPortInputStream extends InputStream {

    private final SerialPort port;

    public SerialPortInputStream(SerialPort port) {
        this.port = port;
    }

    @Override
    public int read() throws IOException {
        // read should block
        while (available() <= 0) {
            sleep(1);
        }
        try {
            return port.readBytes(1)[0];
        } catch (SerialPortException ex) {
            throw new IOException(ex);
        }
    }

    private void sleep(long milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int available() throws IOException {
        try {
            return port.getInputBufferBytesCount();
        } catch (SerialPortException ex) {
            throw new IOException(ex);
        }
    }

}
