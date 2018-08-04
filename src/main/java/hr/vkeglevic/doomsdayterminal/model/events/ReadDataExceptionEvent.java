package hr.vkeglevic.doomsdayterminal.model.events;

/**
 *
 * @author vanja
 */
public class ReadDataExceptionEvent {

    private final Exception exception;

    public ReadDataExceptionEvent(Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }

}
