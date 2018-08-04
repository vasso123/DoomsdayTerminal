package hr.vkeglevic.doomsdayterminal.model.events;

/**
 *
 * @author vanja
 */
public class SentDataEvent {
    private final byte[] data;

    public SentDataEvent(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
    
    
}
