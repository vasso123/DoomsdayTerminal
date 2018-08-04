package hr.vkeglevic.doomsdayterminal.ui.tasks;

import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import hr.vkeglevic.doomsdayterminal.ui.dialogs.ProgressDialog;
import java.util.Collections;
import java.util.Observable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 *
 * @author vanja
 */
public class BaseBackgroundTask implements Runnable {

    private static final Logger LOG = Logger.getLogger(BaseBackgroundTask.class.getName());

    private final WindowBasedTextGUI textGUI;
    private final Job job;
    private final Runnable onDone;
    private final AtomicBoolean taskCancelled = new AtomicBoolean();
    private final ProgressDialog progressDialog;
    private final boolean interruptBackgroundWorkOnCancel;

    public static interface Job {

        void job() throws Exception;
    }

    public BaseBackgroundTask(
            WindowBasedTextGUI textGUI,
            Job job,
            Runnable onDone,
            Runnable onCancel,
            Observable observableProgress,
            boolean interruptBackgroundWorkOnCancel
    ) {
        this.textGUI = textGUI;
        this.job = job;
        this.onDone = onDone;
        this.interruptBackgroundWorkOnCancel = interruptBackgroundWorkOnCancel;
        progressDialog = initProgressDialog(onCancel, observableProgress);
    }

    private ProgressDialog initProgressDialog(Runnable onCancel, Observable observableProgress) {
        final boolean determinateProgres = observableProgress != null;
        ProgressDialog dialog = ProgressDialog.createDialog(
                "Progress",
                "Please wait...",
                determinateProgres,
                onCancel == null ? null : (abortButton) -> {
                            taskCancelled.set(true);
                            onCancel.run();
                        }
        );
        dialog.setHints(Collections.singletonList(Window.Hint.CENTERED));
        if (observableProgress != null) {
            observableProgress.addObserver(dialog);
        }
        return dialog;
    }

    @Override
    public void run() {
        try {
            if (interruptBackgroundWorkOnCancel) {
                final Thread backgrundThread = Thread.currentThread();
                progressDialog.addAbortListener((b) -> {
                    backgrundThread.interrupt();
                });
            }
            invokeOnGuiThread(() -> progressDialog.showDialog(textGUI, false));
            job.job();
            invokeOnGuiThread(() -> progressDialog.close());
            if (onDone != null && !taskCancelled.get()) {
                invokeOnGuiThread(onDone);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "", e);
            invokeOnGuiThread(() -> {
                progressDialog.close();
                MessageDialog.showMessageDialog(
                        textGUI,
                        "Error",
                        StringUtils.substring(ExceptionUtils.getStackTrace(e), 0, 500)
                );
            });
        }
    }

    private void invokeOnGuiThread(Runnable r) {
        textGUI.getGUIThread().invokeLater(r);
    }

}
