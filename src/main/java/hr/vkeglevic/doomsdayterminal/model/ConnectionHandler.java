package hr.vkeglevic.doomsdayterminal.model;

import hr.vkeglevic.doomsdayterminal.model.events.SendingFileEvent;
import hr.vkeglevic.doomsdayterminal.model.events.ReadDataEvent;
import hr.vkeglevic.doomsdayterminal.model.events.ReadDataExceptionEvent;
import hr.vkeglevic.doomsdayterminal.model.events.SentDataEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    private static final int SEND_EVENT_INTERVAL_MS = 200;
    private static final Logger LOG = Logger.getLogger(ConnectionHandler.class.getName());

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
        sendEvent(new SentDataEvent(sentDataStream.toByteArray()));
    }

    public void send(File file) throws IOException, FileNotFoundException, InterruptedException {
        sendFileToConnection(file);
        sentDataStream.write(("\nFile sent: " + file.getAbsolutePath() + "\n").getBytes());
        sendEvent(new SentDataEvent(sentDataStream.toByteArray()));
    }

    private void sendFileToConnection(File file) throws IOException, FileNotFoundException, InterruptedException {
        final long fileSize = file.length();
        final long startTime = System.currentTimeMillis();
        FileInputStream input = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        long totalBytesSentCount = 0;
        sendSendingFileEvent(startTime, fileSize, totalBytesSentCount);
        long timeFromLastUpdate = System.currentTimeMillis();
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1 && connected) {
            connection.getOutputStream().write(buffer, 0, bytesRead);
            totalBytesSentCount += bytesRead;
            if (isTimeForGuiUpdate(timeFromLastUpdate)) {
                sendSendingFileEvent(startTime, fileSize, totalBytesSentCount);
                timeFromLastUpdate = System.currentTimeMillis();
            }
            checkInterrupted();
        }
    }

    private boolean isTimeForGuiUpdate(long timeFromLastUpdate) {
        return System.currentTimeMillis() - timeFromLastUpdate > SEND_EVENT_INTERVAL_MS;
    }

    private void checkInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            LOG.info("Sending file interrupted, aborting...");
            throw new InterruptedException("Sending file aborted");
        }
    }

    public String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private void sendSendingFileEvent(
            final long startTime,
            final long fileSize,
            final long totalBytesSentCount
    ) {
        final long currentSendTime = System.currentTimeMillis() - startTime;
        final long bytesPerSecondSpeed
                = currentSendTime == 0
                        ? 0
                        : (totalBytesSentCount / currentSendTime * 1000);
        final String progressMsg = humanReadableByteCount(totalBytesSentCount, true)
                + " / "
                + humanReadableByteCount(fileSize, true)
                + " - "
                + humanReadableByteCount(bytesPerSecondSpeed, true) + "/s";
        sendEvent(new SendingFileEvent(fileSize, totalBytesSentCount, progressMsg));
    }

    private void readLoop() {
        try {
            InputStream is = connection.getInputStream();
            final int readBufferSize = 100;
            final byte[] readBuffer = new byte[readBufferSize];
            int readBytesCount = 0;
            int readIntoBuffer = 0;
            long timeFromLastUpdate = System.currentTimeMillis();
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
                // we don't want to send the read event too often, it could block the GUI
                if (isTimeForGuiUpdate(timeFromLastUpdate)) {
                    sendReadEvent(currentData);
                    timeFromLastUpdate = System.currentTimeMillis();
                }
            }
        } catch (Exception ex) {
            sendEvent(new ReadDataExceptionEvent(ex));
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

    private void sendEvent(Object event) {
        setChanged();
        notifyObservers(event);
    }

    private void sendReadEvent(final byte[] currentData) {
        sendEvent(new ReadDataEvent(currentData));
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
