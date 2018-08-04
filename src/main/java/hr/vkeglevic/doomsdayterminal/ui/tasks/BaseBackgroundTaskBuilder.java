package hr.vkeglevic.doomsdayterminal.ui.tasks;

import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import java.util.Observable;

public class BaseBackgroundTaskBuilder {

    private WindowBasedTextGUI textGUI;
    private BaseBackgroundTask.Job job;
    private Runnable onDone;
    private Runnable onCancel;
    private Observable observableProgress;
    private boolean interruptBackgroundWorkOnCancel;

    public BaseBackgroundTaskBuilder() {
    }

    public BaseBackgroundTaskBuilder setTextGUI(WindowBasedTextGUI textGUI) {
        this.textGUI = textGUI;
        return this;
    }

    public BaseBackgroundTaskBuilder setJob(BaseBackgroundTask.Job job) {
        this.job = job;
        return this;
    }

    public BaseBackgroundTaskBuilder setOnDone(Runnable onDone) {
        this.onDone = onDone;
        return this;
    }

    public BaseBackgroundTaskBuilder setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
        return this;
    }

    public BaseBackgroundTaskBuilder setObservableProgress(Observable observableProgress) {
        this.observableProgress = observableProgress;
        return this;
    }

    public BaseBackgroundTaskBuilder setInterruptBackgroundWorkOnCancel(boolean interruptBackgroundWorkOnCancel) {
        this.interruptBackgroundWorkOnCancel = interruptBackgroundWorkOnCancel;
        return this;
    }

    public BaseBackgroundTask createBaseBackgroundTask() {
        return new BaseBackgroundTask(
                textGUI,
                job,
                onDone,
                onCancel,
                observableProgress,
                interruptBackgroundWorkOnCancel
        );
    }

}
