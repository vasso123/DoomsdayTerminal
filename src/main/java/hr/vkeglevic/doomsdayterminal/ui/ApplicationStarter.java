package hr.vkeglevic.doomsdayterminal.ui;

import hr.vkeglevic.doomsdayterminal.ui.views.MainView;
import hr.vkeglevic.doomsdayterminal.ui.controllers.MainController;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.TextGUIThread;
import static com.googlecode.lanterna.gui2.Window.Hint.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.MouseCaptureMode;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 *
 * @author vanja
 */
public class ApplicationStarter {

    private static final String GUI_THREAD_NAME = "lanterna-gui-thread";
    private static final String APP_NAME = "Doomsday Terminal";

    private DefaultTerminalFactory terminalFactory;
    private Terminal terminal;
    private TerminalScreen screen;
    private MultiWindowTextGUI textGUI;
    private BasicWindow window;

    private void initGui() throws IOException {
        terminalFactory = new DefaultTerminalFactory();
        terminalFactory.setTerminalEmulatorTitle(APP_NAME);
        terminalFactory.setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE);
        terminalFactory.setInitialTerminalSize(new TerminalSize(160, 43));
        terminal = terminalFactory.createTerminal();
        screen = new TerminalScreen(terminal);
        screen.startScreen();
        textGUI = new MultiWindowTextGUI(screen);
        window = new BasicWindow();
        window.setHints(Arrays.asList(FULL_SCREEN,NO_DECORATIONS, NO_POST_RENDERING));
    }

    public void showMainView() throws IOException {
        initGui();
        MainView tcpClientView = new MainView(window, terminal.getTerminalSize());
        MainController mainController = new MainController(tcpClientView);
        mainController.init();
        setDefaultUncaughtExceptionHandler();
        textGUI.addWindowAndWait(window);
        screen.stopScreen();
        mainController.shutdownApplication();
    }

    private void setDefaultUncaughtExceptionHandler() {
        textGUI.getGUIThread().setExceptionHandler(new TextGUIThread.ExceptionHandler() {
            @Override
            public boolean onIOException(IOException e) {
                handleUnhandledExceptions(e);
                return false;
            }

            @Override
            public boolean onRuntimeException(RuntimeException e) {
                handleUnhandledExceptions(e);
                return false;
            }
        });
        Thread.currentThread().setName(GUI_THREAD_NAME);
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
            if (t != null && GUI_THREAD_NAME.equals(t.getName())) {
                handleUnhandledExceptions(e);
            }
        });
    }

    private void handleUnhandledExceptions(Throwable e) {
        MessageDialog.showMessageDialog(
                textGUI,
                "Unhandled exception",
                StringUtils.substring(ExceptionUtils.getStackTrace(e), 0, 500)
        );
    }

}
