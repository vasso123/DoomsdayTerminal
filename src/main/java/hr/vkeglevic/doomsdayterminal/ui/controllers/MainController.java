package hr.vkeglevic.doomsdayterminal.ui.controllers;

import hr.vkeglevic.doomsdayterminal.util.ByteUtils;
import com.googlecode.lanterna.gui2.Button;
import static com.googlecode.lanterna.gui2.Button.Listener;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window.Hint;
import com.googlecode.lanterna.gui2.dialogs.FileDialog;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import hr.vkeglevic.doomsdayterminal.model.ConnectionHandler;
import hr.vkeglevic.doomsdayterminal.model.events.ReadDataEvent;
import hr.vkeglevic.doomsdayterminal.model.events.ReadDataExceptionEvent;
import hr.vkeglevic.doomsdayterminal.model.events.SentDataEvent;
import hr.vkeglevic.doomsdayterminal.ui.views.DataPanel;
import hr.vkeglevic.doomsdayterminal.ui.tasks.BaseBackgroundTaskBuilder;
import hr.vkeglevic.doomsdayterminal.ui.views.MainView;
import hr.vkeglevic.doomsdayterminal.ui.views.SendDataPanel;
import hr.vkeglevic.doomsdayterminal.util.ClipboardUtils;
import java.awt.HeadlessException;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 *
 * @author vanja
 */
public class MainController {

    private static final Logger LOG = Logger.getLogger(MainController.class.getName());

    private final MainView mainView;

    private final Listener connectButtonListener = showErrorInDialog(this::handleConnectButtonClick);
    private final Listener sendFileButtonListener = showErrorInDialog(this::handleSendFileButtonClick);
    private final Listener closeAppButtonListener = showErrorInDialog(this::handleCloseAppButtonClick);
    private final Listener sendButtonListener = showErrorInDialog(this::handleSendButtonClick);
    private final Listener pasteButtonListener = showErrorInDialog(this::handlePasteButtonClick);
    private final Listener clearReceivedDataButtonListener = showErrorInDialog(this::handleClearReceivedDataButtonClick);
    private final Listener clearSentDataButtonListener = showErrorInDialog(this::handleClearSentDataButtonClick);
    private final Listener copyReceivedDataButtonListener = showErrorInDialog(this::handleCopyReceivedDataButtonClick);
    private final Listener copySentDataButtonListener = showErrorInDialog(this::handleCopySentDataButtonClick);

    private final Observer connectionObserver = this::handleConnectionData;
    private final ExecutorService taskExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService abortTaskExecutor = Executors.newSingleThreadExecutor();

    private ConnectionHandler connectionHandler;
    private final ConnectionSettingsController connectionSettingsController;

    public MainController(MainView mainView) {
        this.mainView = mainView;
        connectionSettingsController = new ConnectionSettingsController();
    }

    public void init() {
        LOG.info("Init...");
        connectionSettingsController.init();
        mainView.init(connectionSettingsController.getView());
        mainView.getConnectButton().takeFocus();
        initListeners();
    }

    private void initListeners() {
        mainView.setConnectButtonListener(connectButtonListener);
        mainView.setSendFileButtonListener(sendFileButtonListener);
        mainView.setCloseAppButtonListener(closeAppButtonListener);
        getSendDataPanels().forEach((sdp) -> {
            sdp.setSendButtonListener(sendButtonListener);
            sdp.setPasteButtonListener(pasteButtonListener);
        });
        DataPanel receivedDataPanel = mainView.getReceivedDataPanel();
        receivedDataPanel.getClearDataBTN().addListener(clearReceivedDataButtonListener);
        receivedDataPanel.getCopyToClpbrdBTN().addListener(copyReceivedDataButtonListener);

        DataPanel sentDataPanel = mainView.getSentDataPanel();
        sentDataPanel.getClearDataBTN().addListener(clearSentDataButtonListener);
        sentDataPanel.getCopyToClpbrdBTN().addListener(copySentDataButtonListener);

    }

    private void removeListeners() {
        mainView.removeConnectButtonListener(connectButtonListener);
    }

    private void handleConnectButtonClick(Button b) {
        LOG.info("connection handler: " + Objects.toString(connectionHandler));
        if (connectionHandler != null && connectionHandler.isConnected()) {
            LOG.info("Disconnecting...");
            taskExecutor.submit(
                    new BaseBackgroundTaskBuilder()
                            .setTextGUI(mainView.getWindow().getTextGUI())
                            .setJob(() -> {
                                connectionHandler.deleteObserver(connectionObserver);
                                connectionHandler.disconnect();
                            })
                            .setOnDone(() -> mainView.setConnectButtonLabel())
                            .createBaseBackgroundTask()
            );
        } else {
            LOG.info("Connecting...");
            connectionHandler = new ConnectionHandler(
                    connectionSettingsController.getConnection()
            );
            connectionHandler.addObserver(connectionObserver);

            taskExecutor.submit(
                    new BaseBackgroundTaskBuilder()
                            .setTextGUI(mainView.getWindow().getTextGUI())
                            .setJob(connectionHandler::connect)
                            .setOnDone(() -> mainView.setDisconnectButtonLabel())
                            .setOnCancel(() -> {
                                abortTaskExecutor.submit(() -> {
                                    try {
                                        connectionHandler.disconnect();
                                    } catch (IOException ex) {
                                    }
                                });
                            })
                            .createBaseBackgroundTask()
            );
        }
    }

    private void handleCloseAppButtonClick(Button b) {
        shutdownApplication();
    }

    private void handleSendFileButtonClick(Button b) {
        if (connectionHandler != null && connectionHandler.isConnected()) {
            FileDialogBuilder fileDialogBuilder = new FileDialogBuilder()
                    .setActionLabel("Choose file to send")
                    .setExtraWindowHints(new HashSet<>(Arrays.asList(Hint.CENTERED)));
            fileDialogBuilder.setShowHiddenDirectories(true);
            FileDialog fileDialog = fileDialogBuilder.build();
            File chosenFile = fileDialog.showDialog(mainView.getWindow().getTextGUI());
            if (chosenFile != null) {
                taskExecutor.submit(new BaseBackgroundTaskBuilder()
                        .setTextGUI(mainView.getWindow().getTextGUI())
                        .setJob(() -> connectionHandler.send(chosenFile))
                        .setObservableProgress(connectionHandler)
                        .setInterruptBackgroundWorkOnCancel(true)
                        .createBaseBackgroundTask());
            }
        }
    }

    public void shutdownApplication() {
        if (connectionHandler != null && connectionHandler.isConnected()) {
            taskExecutor.submit(() -> {
                connectionHandler.disconnect();
                return null;
            });
        }
        taskExecutor.shutdown();
        abortTaskExecutor.shutdown();
        mainView.getWindow().close();
    }

    private void handleConnectionData(Observable o, Object arg) {
        mainView.getWindow().getTextGUI().getGUIThread().invokeLater(() -> {
            if (arg instanceof ReadDataExceptionEvent) {
                ReadDataExceptionEvent event = (ReadDataExceptionEvent) arg;
                MessageDialog.showMessageDialog(
                        mainView.getWindow().getTextGUI(),
                        "Error",
                        event.getException().getMessage()
                );
                if (connectionHandler != null && !connectionHandler.isConnected()) {
                    mainView.setConnectButtonLabel();
                }
            } else if (arg instanceof ReadDataEvent) {
                ReadDataEvent event = (ReadDataEvent) arg;
                showDataOnPanel(mainView.getReceivedDataPanel(), new String(event.getData()));
            } else if (arg instanceof SentDataEvent) {
                SentDataEvent event = (SentDataEvent) arg;
                showDataOnPanel(mainView.getSentDataPanel(), new String(event.getData()));
            }
        });
    }

    private void handleSendButtonClick(Button b) {
        SendDataPanel sendDataPanel = getPanelByButton(b, SendDataPanel::getSendBTN);
        final String dataToSend = sendDataPanel.getDataToSend();
        if (StringUtils.isNotEmpty(dataToSend)) {
            taskExecutor.submit(
                    new BaseBackgroundTaskBuilder()
                            .setTextGUI(mainView.getWindow().getTextGUI())
                            .setJob(() -> {
                                if (sendDataPanel.getSendHexCB().isChecked()) {
                                    connectionHandler.send(ByteUtils.hexStringToByteArray(dataToSend));
                                } else {
                                    connectionHandler.send(dataToSend.getBytes());
                                }
                            })
                            .createBaseBackgroundTask()
            );
        }
    }

    private void handlePasteButtonClick(Button b) {
        try {
            String pasteFromClipboard = ClipboardUtils.pasteFromClipboard();
            if (StringUtils.isNotEmpty(pasteFromClipboard)) {
                SendDataPanel sendDataPanel = getPanelByButton(b, SendDataPanel::getPasteBTN);
                TextBox textBox = sendDataPanel.getDataToSendTB();
                textBox.setText(ObjectUtils.defaultIfNull(textBox.getText(), "") + pasteFromClipboard);
            }
        } catch (IOException | UnsupportedFlavorException e) {
            showErrorWhileAccessingClipboard(e);
        }
    }

    private void copyToClipboard(String text) {
        try {
            ClipboardUtils.copyToClipboard(text);
        } catch (HeadlessException e) {
            showErrorWhileAccessingClipboard(e);
        }
    }

    private void showErrorWhileAccessingClipboard(Exception e) {
        MessageDialog.showMessageDialog(
                mainView.getWindow().getTextGUI(),
                "Clipboard not available",
                StringUtils.substring(ExceptionUtils.getStackTrace(e), 0, 500)
        );
    }

    private void handleClearReceivedDataButtonClick(Button b) {
        taskExecutor.submit(
                new BaseBackgroundTaskBuilder()
                        .setTextGUI(mainView.getWindow().getTextGUI())
                        .setJob(() -> connectionHandler.clearReadData())
                        .setOnDone(() -> mainView.getReceivedDataPanel().getDataTB().setText(""))
                        .createBaseBackgroundTask()
        );
    }

    private void handleClearSentDataButtonClick(Button b) {
        taskExecutor.submit(
                new BaseBackgroundTaskBuilder()
                        .setTextGUI(mainView.getWindow().getTextGUI())
                        .setJob(() -> connectionHandler.clearSentData())
                        .setOnDone(() -> mainView.getSentDataPanel().getDataTB().setText(""))
                        .createBaseBackgroundTask()
        );
    }

    private void handleCopyReceivedDataButtonClick(Button b) {
        if (connectionHandler != null) {
            copyToClipboard(new String(connectionHandler.getReceivedData()));
        }
    }

    private void handleCopySentDataButtonClick(Button b) {
        if (connectionHandler != null) {
            copyToClipboard(new String(connectionHandler.getSentData()));
        }
    }

    private SendDataPanel getPanelByButton(Button b, Function<SendDataPanel, Button> getButtonFunction) {
        List<SendDataPanel> sendDataPanels = getSendDataPanels();
        for (SendDataPanel sendDataPanel : sendDataPanels) {
            if (getButtonFunction.apply(sendDataPanel) == b) {
                return sendDataPanel;
            }
        }
        throw new RuntimeException("BUG, error while searching for send data panel!");
    }

    private List<SendDataPanel> getSendDataPanels() {
        return Arrays.asList(
                mainView.getSendDataPanel1(),
                mainView.getSendDataPanel2(),
                mainView.getSendDataPanel3()
        );
    }

    private void showDataOnPanel(DataPanel dataPanel, String data) {
        if (dataPanel.getShowDataAsHexCB().isChecked()) {
            data = decorateHexString(ByteUtils.bytesToHex(data.getBytes()));
        }
        // workaround for a lanterna bug, when setting only the nl character to textbox
        if (!"\n".equals(data)) {
            dataPanel.getDataTB().setText(data);
        }
    }

    private String decorateHexString(String hexString) {
        final int chunkLength = 2;
        return IntStream.range(0, hexString.length() / chunkLength)
                .map((i) -> i * 2)
                .mapToObj((i) -> hexString.substring(i, i + chunkLength))
                .map((chunk) -> "{" + chunk + "}")
                .reduce("", String::concat);
    }

    private Listener showErrorInDialog(Listener listener) {
        return (button) -> {
            try {
                LOG.log(Level.FINE, "User clicked button: {0}", button);
                listener.onTriggered(button);
            } catch (Exception e) {
                handleUnhandledException(e);
            }
        };
    }

    public void handleUnhandledException(Throwable e) {
        LOG.log(Level.SEVERE, "Unhandled exception", e);
        MessageDialog.showMessageDialog(
                mainView.getWindow().getTextGUI(),
                "Unhandled exception",
                StringUtils.substring(ExceptionUtils.getStackTrace(e), 0, 500)
        );
    }

}
