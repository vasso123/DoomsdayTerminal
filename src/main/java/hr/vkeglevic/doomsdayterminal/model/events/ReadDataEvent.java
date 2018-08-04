package hr.vkeglevic.doomsdayterminal.model.events;

/**
 *
 * @author vanja
 */
public class ReadDataEvent {
    private final byte[] data;

    public ReadDataEvent(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
    
    
}
