package hr.vkeglevic.doomsdayterminal.model;

import hr.vkeglevic.doomsdayterminal.model.events.Event;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author vanja
 */
public class ConnectionHandler extends Observable {

    private final Connection connection;
    private final ExecutorService readExecutorService;
    private volatile boolean connected;
    private volatile boolean disconnectStarted;
    private final ByteArrayOutputStream readDataStream = new ByteArrayOutputStream();
    private final ByteArrayOutputStream sentDataStream = new ByteArrayOutputStream();

    public ConnectionHandler(Connection connection) {
        this.connection = connection;
        readExecutorService = Executors.newSingleThreadExecutor();
    }

    public boolean isConnected() {
        return connected;
    }

    public void disconnect() throws IOException {
        if (connection != null) {
            connected = false;
            disconnectStarted = true;
            connection.close();
        }
    }

    public void connect() {
        try {
            connection.open();
            connected = true;
            readExecutorService.submit(() -> readLoop());
        } catch (Exception e) {
            if (!disconnectStarted) {
                throw new RuntimeException(e);
            }
        }
    }

    public void send(byte[] data) throws IOException {
        connection.getOutputStream().write(data);
        sentDataStream.write(data);
        setChanged();
        notifyObservers(Event.sentDataEvent(sentDataStream.toByteArray()));
    }

    private void readLoop() {
        try {
            InputStream is = connection.getInputStream();
            while (connected) {
                final int read = is.read();
                if (read == -1) {
                    throw new RuntimeException("Disconnected");
                }
                readDataStream.write(read);
                while (is.available() > 0) {
                    readDataStream.write(is.read());
                }
                setChanged();
                notifyObservers(Event.readDataEvent(readDataStream.toByteArray()));
            }
        } catch (IOException ex) {
            setChanged();
            notifyObservers(Event.exceptionEvent(ex));
        }
    }

    public void clearReadData() {
        readDataStream.reset();
    }

    public void clearSentData() {
        sentDataStream.reset();
    }

    public byte[] getSentData() {
        return sentDataStream.toByteArray();
    }

    public byte[] getReceivedData() {
        return readDataStream.toByteArray();
    }

}
