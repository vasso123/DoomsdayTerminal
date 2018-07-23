package hr.vkeglevic.doomsdayterminal.model.events;

/**
 *
 * @author vanja
 */
public class Event {

    private final Exception exception;
    private final byte[] readData;
    private final byte[] sentData;

    public Event(Exception exception, byte[] readData, byte[] sentData) {
        this.exception = exception;
        this.readData = readData;
        this.sentData = sentData;
    }

    public Exception getException() {
        return exception;
    }

    public byte[] getReadData() {
        return readData;
    }

    public byte[] getSentData() {
        return sentData;
    }

    public static Event readDataEvent(byte[] readData) {
        return new Event(null, readData, null);
    }

    public static Event sentDataEvent(byte[] sentData) {
        return new Event(null, null, sentData);
    }

    public static Event exceptionEvent(Exception e) {
        return new Event(e, null, null);
    }

}
