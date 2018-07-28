package hr.vkeglevic.doomsdayterminal;

import hr.vkeglevic.doomsdayterminal.ui.views.MainView;
import hr.vkeglevic.doomsdayterminal.ui.controllers.MainController;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.TextGUIThread;
import static com.googlecode.lanterna.gui2.Window.Hint.*;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.MouseCaptureMode;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author vanja
 */
public class ApplicationStarter {

    private static final String GUI_THREAD_NAME = "lanterna-gui-thread";
    private static final String APP_NAME = "Doomsday Terminal";
    // TODO log level should come from configuration
    private static final Level LOG_LEVEL = Level.parse("FINE");

    private static final Logger LOG = Logger.getLogger(ApplicationStarter.class.getName());

    private DefaultTerminalFactory terminalFactory;
    private Terminal terminal;
    private TerminalScreen screen;
    private MultiWindowTextGUI textGUI;
    private BasicWindow window;
    private boolean runningInConsole;

    private void initLanternaGui() throws IOException {
        LOG.fine("Init gui start...");
        terminalFactory = new DefaultTerminalFactory();
        terminalFactory.setTerminalEmulatorTitle(APP_NAME);
        terminalFactory.setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE);
        terminalFactory.setInitialTerminalSize(new TerminalSize(160, 43));
        // we need this so we can set a custom jframe icon
        terminalFactory.setAutoOpenTerminalEmulatorWindow(false);
        terminal = terminalFactory.createTerminal();
        LOG.info("Terminal instance: " + terminal);
        runningInConsole = !StringUtils.containsAny(
                terminal.getClass().getName().toLowerCase(),
                "SWING",
                "AWT"
        );
        LOG.fine("runningInConsole: " + runningInConsole);
        if (terminal instanceof SwingTerminalFrame) {
            LOG.fine("Setting the app icon...");
            SwingTerminalFrame frame = (SwingTerminalFrame) terminal;
            // TODO add an application icon
//            frame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/explosion.png")));
            frame.setVisible(true);
        }
        screen = new TerminalScreen(terminal);
        screen.startScreen();
        textGUI = new MultiWindowTextGUI(screen);
        window = new BasicWindow();
        window.setHints(Arrays.asList(FULL_SCREEN, NO_DECORATIONS, NO_POST_RENDERING));
        LOG.fine("Init gui end.");
    }

    private void setDefaultUncaughtExceptionHandler(MainController mainController) {
        textGUI.getGUIThread().setExceptionHandler(new TextGUIThread.ExceptionHandler() {
            @Override
            public boolean onIOException(IOException e) {
                mainController.handleUnhandledException(e);
                return false;
            }

            @Override
            public boolean onRuntimeException(RuntimeException e) {
                mainController.handleUnhandledException(e);
                return false;
            }
        });
        Thread.currentThread().setName(GUI_THREAD_NAME);
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
            if (t != null && GUI_THREAD_NAME.equals(t.getName())) {
                mainController.handleUnhandledException(e);
            }
        });
    }

    private void addConsoleLogHandler() throws SecurityException {
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(LOG_LEVEL);
        Logger.getGlobal().addHandler(consoleHandler);
    }

    private void initLogging() throws IOException {
        final String logDirectory = "logs";
        File directory = new File(logDirectory);
        if (!directory.exists()) {
            directory.mkdir();
        }

        final Logger root = Logger.getLogger("");
        root.setLevel(LOG_LEVEL);

        // first, remove console handler, as we might be in a console and break GUI
        for (Handler handler : root.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                root.removeHandler(handler);
            }
        }

        FileHandler fileHandler = new FileHandler(
                logDirectory + "/doomsdayterminal.log",
                100000,
                5,
                true
        );
        fileHandler.setFormatter(new SimpleFormatter());
        fileHandler.setLevel(LOG_LEVEL);
        root.addHandler(fileHandler);
    }

    private void showMainView() throws IOException {
        MainView tcpClientView = new MainView(window, terminal.getTerminalSize());
        MainController mainController = new MainController(tcpClientView);
        mainController.init();
        setDefaultUncaughtExceptionHandler(mainController);

        LOG.info("Giving control to the Lanterna GUI thread...");
        textGUI.addWindowAndWait(window);
        screen.stopScreen();

        LOG.info("GUI thread finished, shutting down...");
        mainController.shutdownApplication();
    }

    public void startApplication() throws IOException, InterruptedException {
        initLogging();
        initLanternaGui();
        if (!runningInConsole) {
            addConsoleLogHandler();
        }
        // this call will block as lanterna will use this thread as gui thread
        showMainView();
        // gui is closed, give some time for other threads to finish their work and exit
        Thread.sleep(100);
        System.exit(0);
    }
}
