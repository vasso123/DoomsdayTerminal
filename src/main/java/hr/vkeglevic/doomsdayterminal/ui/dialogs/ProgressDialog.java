package hr.vkeglevic.doomsdayterminal.ui.dialogs;

import com.googlecode.lanterna.gui2.AnimatedLabel;
import com.googlecode.lanterna.gui2.Borders;
import static com.googlecode.lanterna.gui2.Borders.*;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import static com.googlecode.lanterna.gui2.LinearLayout.*;
import static com.googlecode.lanterna.gui2.LinearLayout.Alignment.*;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.ProgressBar;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.DialogWindow;
import hr.vkeglevic.doomsdayterminal.model.events.WorkProgressEvent;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.*;

/**
 *
 * @author vanja
 */
public class ProgressDialog extends DialogWindow implements Observer {

    private static final int progressBarLength = 30;

    private final Button abortBTN;
    private Optional<ProgressBar> determinateProgressBar = Optional.empty();
    private Optional<Label> progressMessageLabel = Optional.empty();

    private ProgressDialog(String title, String text, boolean determinateProgress) {
        super(title);
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        panel.addComponent(new EmptySpace(), createLayoutData(Center));
        panel.addComponent(new Label(text), createLayoutData(Center));
        if (determinateProgress) {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setPreferredWidth(progressBarLength);
            panel.addComponent(progressBar.withBorder(singleLine()), createLayoutData(Center));
            determinateProgressBar = Optional.of(progressBar);
            Label progressMsg = new Label("");
            panel.addComponent(progressMsg, createLayoutData(Center));
            progressMessageLabel = Optional.of(progressMsg);
        } else {
            panel.addComponent(createSwingLikeProgressBar().withBorder(singleLine()), createLayoutData(Center));
        }
        abortBTN = new Button("Abort");
        panel.addComponent(abortBTN.withBorder(Borders.singleLineBevel()), createLayoutData(Center));
        panel.addComponent(new EmptySpace(), createLayoutData(Center));
        setComponent(panel);
    }

    private AnimatedLabel createSwingLikeProgressBar() {
        // first we'll generate all the frames
        final String movingBar = "▉▉▉▉▉▉▉";
        final String empty = " ";
        List<String> frames = IntStream.range(0, progressBarLength)
                .mapToObj((index) -> repeat(empty, index)
                + movingBar
                + repeat(empty, progressBarLength - movingBar.length() - index)
                ).collect(Collectors.toList());
        // now we add them first from left to right, then the opposite
        AnimatedLabel animatedLabel = new AnimatedLabel(frames.get(0));
        IntStream.range(1, frames.size())
                .forEach((i) -> animatedLabel.addFrame(frames.get(i)));
        IntStream.range(1, frames.size() - 2)
                .boxed()
                .sorted(Collections.reverseOrder())
                .forEach((i) -> animatedLabel.addFrame(frames.get(i)));
        animatedLabel.startAnimation(50);
        return animatedLabel;
    }

    @Override
    public Object showDialog(WindowBasedTextGUI textGUI) {
        showDialog(textGUI, true);
        return null;
    }

    public void showDialog(WindowBasedTextGUI textGUI, boolean blockUntilClosed) {
        textGUI.addWindow(this);

        if (blockUntilClosed) {
            waitUntilClosed();
        }
    }

    public void addAbortListener(Button.Listener listener) {
        abortBTN.addListener(listener);
    }

    public static ProgressDialog createDialog(
            String title,
            String text,
            boolean determinateProgress,
            Button.Listener abortListener
    ) {
        final ProgressDialog progressDialog = new ProgressDialog(title, text, determinateProgress);
        final Button.Listener closeDialog = (button) -> progressDialog.close();
        if (abortListener == null) {
            progressDialog.addAbortListener(closeDialog);
        } else {
            progressDialog.addAbortListener(abortListener);
        }
        return progressDialog;
    }

    private void update(WorkProgressEvent event) {
        determinateProgressBar.ifPresent((ProgressBar bar) -> {
            bar.setMin(0);
            long totalWork = event.getTotalWork();
            long currentlyDoneWork = event.getCurrentlyDoneWork();
            double percentageDone = ((double) currentlyDoneWork / totalWork) * 100;
            bar.setMax(100);
            bar.setValue((int) percentageDone);
            if (StringUtils.isNotEmpty(event.getWorkProgressMessage())) {
                progressMessageLabel.get().setText(event.getWorkProgressMessage());
            }
        });
    }

    @Override
    public void update(Observable o, Object event) {
        if (event instanceof WorkProgressEvent) {
            update((WorkProgressEvent) event);
        }
    }

}
