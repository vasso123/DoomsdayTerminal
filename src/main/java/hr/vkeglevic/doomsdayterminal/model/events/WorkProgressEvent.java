package hr.vkeglevic.doomsdayterminal.model.events;

/**
 *
 * @author vanja
 */
public class WorkProgressEvent {

    private final long totalWork;
    private final long currentlyDoneWork;
    private final String workProgressMessage;

    public WorkProgressEvent(long totalWork, long currentlyDoneWork, String workProgressMessage) {
        this.totalWork = totalWork;
        this.currentlyDoneWork = currentlyDoneWork;
        this.workProgressMessage = workProgressMessage;
    }

    public long getTotalWork() {
        return totalWork;
    }

    public long getCurrentlyDoneWork() {
        return currentlyDoneWork;
    }

    public String getWorkProgressMessage() {
        return workProgressMessage;
    }

}
