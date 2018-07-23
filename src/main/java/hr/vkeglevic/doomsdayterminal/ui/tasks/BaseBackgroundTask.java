package hr.vkeglevic.doomsdayterminal.ui.tasks;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.WaitingDialog;
import javax.xml.ws.Holder;

/**
 *
 * @author vanja
 */
public class BaseBackgroundTask implements Runnable {

    private final WindowBasedTextGUI textGUI;
    private final Job job;
    private final Runnable onDone;

    public static interface Job {

        void job() throws Exception;
    }

    public BaseBackgroundTask(WindowBasedTextGUI textGUI, Job job, Runnable onDone) {
        this.textGUI = textGUI;
        this.job = job;
        this.onDone = onDone;
    }

    public BaseBackgroundTask(WindowBasedTextGUI textGUI, Job job) {
        this(textGUI, job, null);
    }

    @Override
    public void run() {
        Holder<WaitingDialog> holder = new Holder<>();
        try {
            invokeOnGuiThread(() -> {
                WaitingDialog dialog = WaitingDialog.createDialog("Progress", "Please wait...");
                dialog.setPosition(new TerminalPosition(50, 20));
                dialog.showDialog(textGUI, false);
                holder.value = dialog;
            });
            job.job();
            invokeOnGuiThread(() -> dismissProgressDialog(holder));
            if (onDone != null) {
                invokeOnGuiThread(onDone);
            }
        } catch (Exception e) {
            invokeOnGuiThread(() -> {
                dismissProgressDialog(holder);
                MessageDialog.showMessageDialog(
                        textGUI,
                        "Error",
                        e.getMessage()
                );
            });
        }
    }

    private void invokeOnGuiThread(Runnable r) {
        textGUI.getGUIThread().invokeLater(r);
    }

    private void dismissProgressDialog(Holder<WaitingDialog> holder) {
        if (holder.value != null) {
            holder.value.close();
        }
    }

}
