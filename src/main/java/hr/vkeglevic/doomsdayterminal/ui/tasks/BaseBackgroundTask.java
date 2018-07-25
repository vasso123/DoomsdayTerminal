package hr.vkeglevic.doomsdayterminal.ui.tasks;

import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import hr.vkeglevic.doomsdayterminal.ui.dialogs.ProgressDialog;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.mutable.MutableObject;

/**
 *
 * @author vanja
 */
public class BaseBackgroundTask implements Runnable {

    private final WindowBasedTextGUI textGUI;
    private final Job job;
    private final Runnable onDone;
    private final Runnable onCancel;
    private final AtomicBoolean taskCancelled = new AtomicBoolean();

    public static interface Job {

        void job() throws Exception;
    }

    public BaseBackgroundTask(
            WindowBasedTextGUI textGUI,
            Job job,
            Runnable onDone,
            Runnable onCancel
    ) {
        this.textGUI = textGUI;
        this.job = job;
        this.onDone = onDone;
        this.onCancel = onCancel;
    }

    public BaseBackgroundTask(
            WindowBasedTextGUI textGUI,
            Job job,
            Runnable onDone
    ) {
        this(textGUI, job, onDone, null);
    }

    public BaseBackgroundTask(WindowBasedTextGUI textGUI, Job job) {
        this(textGUI, job, null, null);
    }

    @Override
    public void run() {
        MutableObject<ProgressDialog> progressDialogHolder = new MutableObject();
        try {
            invokeOnGuiThread(() -> {
                ProgressDialog dialog = ProgressDialog.createDialog(
                        "Progress",
                        "Please wait...",
                        onCancel == null ? null : (button) -> {
                                    taskCancelled.set(true);
                                    onCancel.run();
                                }
                );
                dialog.setHints(Collections.singletonList(Window.Hint.CENTERED));
                dialog.showDialog(textGUI, false);
                progressDialogHolder.setValue(dialog);
            });
            job.job();
            invokeOnGuiThread(() -> dismiss(progressDialogHolder));
            if (onDone != null && !taskCancelled.get()) {
                invokeOnGuiThread(onDone);
            }
        } catch (Exception e) {
            invokeOnGuiThread(() -> {
                dismiss(progressDialogHolder);
                MessageDialog.showMessageDialog(
                        textGUI,
                        "Error",
                        e.getMessage()
                );
            });
        }
    }

    private void dismiss(MutableObject<ProgressDialog> dialogHolder) {
        Optional.ofNullable(dialogHolder.getValue()).ifPresent(ProgressDialog::close);
    }

    private void invokeOnGuiThread(Runnable r) {
        textGUI.getGUIThread().invokeLater(r);
    }

}
