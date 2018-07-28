package hr.vkeglevic.doomsdayterminal.model;

import hr.vkeglevic.doomsdayterminal.model.events.Event;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private final int readByteLimit;

    public ConnectionHandler(Connection connection) {
        this.connection = connection;
        readExecutorService = Executors.newSingleThreadExecutor();
        readByteLimit = 10000;
    }

    public boolean isConnected() {
        return connected;
    }

    public void disconnect() throws IOException {
        if (connection != null) {
            connected = false;
            disconnectStarted = true;
            connection.close();
            readExecutorService.shutdownNow();
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
            final int readBufferSize = 100;
            final byte[] readBuffer = new byte[readBufferSize];
            int readBytesCount = 0;
            int readIntoBuffer = 0;
            while (connected) {
                /**
                 * We'll use the blocking api so we don't have to use
                 * Thread.sleep. Chances are that after one byte, there will be
                 * more, so after the first one we'll read in chunks
                 */
                readDataStream.write(waitForFirstByte(is));
                readBytesCount++;
                while (is.available() > 0 && readBytesCount < readByteLimit) {
                    readIntoBuffer = is.read(readBuffer);
                    readDataStream.write(readBuffer, 0, readIntoBuffer);
                    readBytesCount += readIntoBuffer;
                }
                final byte[] currentData = readDataStream.toByteArray();
                checkReadLimit(currentData);
                sendReadEvent(currentData);
            }
        } catch (Exception ex) {
            setChanged();
            notifyObservers(Event.exceptionEvent(ex));
        }
    }

    private int waitForFirstByte(InputStream is) throws IOException {
        final int read = is.read();
        if (read == -1) {
            throw new RuntimeException("Disconnected");
        }
        return read;
    }

    private void checkReadLimit(final byte[] currentData) throws RuntimeException {
        if (currentData.length > readByteLimit) {
            disconnectSilently();
            throw new RuntimeException("Received data too large, please use the log to file option!");
        }
    }

    private void sendReadEvent(final byte[] currentData) {
        setChanged();
        notifyObservers(Event.readDataEvent(currentData));
    }

    private void disconnectSilently() {
        try {
            disconnect();
        } catch (IOException ex) {
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
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

    @Override
    public String toString() {
        return "ConnectionHandler{" + "connection=" + connection + ", connected=" + connected + ", disconnectStarted=" + disconnectStarted + '}';
    }

}
