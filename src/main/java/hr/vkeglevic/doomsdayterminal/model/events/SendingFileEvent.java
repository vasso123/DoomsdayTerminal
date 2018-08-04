package hr.vkeglevic.doomsdayterminal.model.events;

/**
 *
 * @author vanja
 */
public class SendingFileEvent extends WorkProgressEvent {

    public SendingFileEvent(long fileSize, long byteCountSentSoFar, String workProgressMessage) {
        super(fileSize, byteCountSentSoFar, workProgressMessage);
    }

    public SendingFileEvent(long fileSize, long byteCountSentSoFar) {
        this(fileSize, byteCountSentSoFar, null);
    }

}
