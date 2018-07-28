package hr.vkeglevic.doomsdayterminal.infrastructure;

import hr.vkeglevic.doomsdayterminal.model.Connection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author vanja
 */
public class TcpServerConnection implements Connection {

    private final int port;
    private ServerSocket serverSocket;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public TcpServerConnection(int port) {
        this.port = port;
    }

    @Override
    public void open() throws IOException {
        serverSocket = new ServerSocket(port);
        socket = serverSocket.accept();
    }

    @Override
    public void close() throws IOException {
        if (socket != null && socket.isConnected()) {
            socket.close();
        }
        if (serverSocket != null) {
            serverSocket.close();
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
        return "TcpServerConnection{" + "port=" + port + ", serverSocket=" + serverSocket + ", socket=" + socket + '}';
    }

}
