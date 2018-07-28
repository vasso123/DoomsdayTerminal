package hr.vkeglevic.doomsdayterminal.infrastructure;

import hr.vkeglevic.doomsdayterminal.model.Connection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 *
 * @author vanja
 */
public class TcpClientConnection implements Connection {

    private final String ipAddress;
    private final int port;
    private final Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public TcpClientConnection(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
        socket = new Socket();
    }

    @Override
    public void open() throws IOException {
        socket.connect(new InetSocketAddress(ipAddress, port));
    }

    @Override
    public void close() throws IOException {
        if (socket.isConnected()) {
            socket.close();
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (inputStream == null) {
            inputStream = socket.getInputStream();
        }
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (outputStream == null) {
            outputStream = socket.getOutputStream();
        }
        return outputStream;
    }

    @Override
    public String toString() {
        return "TcpClientConnection{" + "ipAddress=" + ipAddress + ", port=" + port + ", socket=" + socket + '}';
    }

}
