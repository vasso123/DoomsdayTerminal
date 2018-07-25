package hr.vkeglevic.doomsdayterminal.ui.controllers;

import hr.vkeglevic.doomsdayterminal.util.ByteUtils;
import com.googlecode.lanterna.gui2.Button;
import static com.googlecode.lanterna.gui2.Button.Listener;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import hr.vkeglevic.doomsdayterminal.model.ConnectionHandler;
import hr.vkeglevic.doomsdayterminal.ui.tasks.BaseBackgroundTask;
import hr.vkeglevic.doomsdayterminal.ui.views.DataPanel;
import hr.vkeglevic.doomsdayterminal.model.events.Event;
import hr.vkeglevic.doomsdayterminal.ui.views.MainView;
import hr.vkeglevic.doomsdayterminal.ui.views.SendDataPanel;
import hr.vkeglevic.doomsdayterminal.util.ClipboardUtils;
import java.awt.HeadlessException;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 *
 * @author vanja
 */
public class MainController {

    private final MainView mainView;

    private final Listener connectButtonListener = showErrorInDialog(this::handleConnectButtonClick);
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
        connectionSettingsController.init();
        mainView.init(connectionSettingsController.getView());
        mainView.getConnectButton().takeFocus();
        initListeners();
    }

    private void initListeners() {
        mainView.setConnectButtonListener(connectButtonListener);
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
        if (connectionHandler != null && connectionHandler.isConnected()) {
            taskExecutor.submit(
                    new BaseBackgroundTask(
                            mainView.getWindow().getTextGUI(),
                            () -> {
                                connectionHandler.deleteObserver(connectionObserver);
                                connectionHandler.disconnect();
                            },
                            () -> mainView.getConnectButton().setLabel("Connect")
                    )
            );
        } else {
            connectionHandler = new ConnectionHandler(
                    connectionSettingsController.getConnection()
            );
            connectionHandler.addObserver(connectionObserver);

            taskExecutor.submit(
                    new BaseBackgroundTask(
                            mainView.getWindow().getTextGUI(),
                            connectionHandler::connect,
                            () -> mainView.getConnectButton().setLabel("Disconnect"),
                            () -> {
                                abortTaskExecutor.submit(() -> {
                                            try {
                                                connectionHandler.disconnect();
                                            } catch (IOException ex) {
                                            }
                                        });
                            }
                    )
            );
        }
    }

    private void handleCloseAppButtonClick(Button b) {
        shutdownApplication();
    }

    public void shutdownApplication() {
        mainView.getWindow().close();
        System.exit(0);
    }

    private void handleConnectionData(Observable o, Object arg) {
        mainView.getWindow().getTextGUI().getGUIThread().invokeLater(() -> {
            Event e = (Event) arg;
            if (e.getException() != null) {
                MessageDialog.showMessageDialog(
                        mainView.getWindow().getTextGUI(),
                        "Error",
                        e.getException().getMessage()
                );
            } else if (e.getReadData() != null) {
                showDataOnPanel(mainView.getReceivedDataPanel(), new String(e.getReadData()));
            } else if (e.getSentData() != null) {
                showDataOnPanel(mainView.getSentDataPanel(), new String(e.getSentData()));
            }
        });
    }

    private void handleSendButtonClick(Button b) {
        SendDataPanel sendDataPanel = getPanelByButton(b, SendDataPanel::getSendBTN);
        final String dataToSend = sendDataPanel.getDataToSend();
        if (StringUtils.isNotEmpty(dataToSend)) {
            taskExecutor.submit(
                    new BaseBackgroundTask(
                            mainView.getWindow().getTextGUI(),
                            () -> {
                                if (sendDataPanel.getSendHexCB().isChecked()) {
                                    connectionHandler.send(ByteUtils.hexStringToByteArray(dataToSend));
                                } else {
                                    connectionHandler.send(dataToSend.getBytes());
                                }
                            }
                    )
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
                new BaseBackgroundTask(
                        mainView.getWindow().getTextGUI(),
                        () -> connectionHandler.clearReadData(),
                        () -> mainView.getReceivedDataPanel().getDataTB().setText("")
                )
        );
    }

    private void handleClearSentDataButtonClick(Button b) {
        taskExecutor.submit(
                new BaseBackgroundTask(
                        mainView.getWindow().getTextGUI(),
                        () -> connectionHandler.clearSentData(),
                        () -> mainView.getSentDataPanel().getDataTB().setText("")
                )
        );
    }

    private void handleCopyReceivedDataButtonClick(Button b) {
        copyToClipboard(new String(connectionHandler.getReceivedData()));
    }

    private void handleCopySentDataButtonClick(Button b) {
        copyToClipboard(new String(connectionHandler.getSentData()));
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
                listener.onTriggered(button);
            } catch (Exception e) {
                MessageDialog.showMessageDialog(
                        mainView.getWindow().getTextGUI(),
                        "Unhandled exception",
                        StringUtils.substring(ExceptionUtils.getStackTrace(e), 0, 500)
                );
            }
        };
    }

}
