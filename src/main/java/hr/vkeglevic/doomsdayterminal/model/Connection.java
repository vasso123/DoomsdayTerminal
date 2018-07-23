package hr.vkeglevic.doomsdayterminal.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author vanja
 */
public interface Connection {

    void open() throws IOException;

    void close() throws IOException;

    InputStream getInputStream() throws IOException;

    OutputStream getOutputStream() throws IOException;
}
