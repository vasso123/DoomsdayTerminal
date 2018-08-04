package hr.vkeglevic.doomsdayterminal.ui.views;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.AbsoluteLayout;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Border;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Button.Listener;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;

/**
 *
 * @author vanja
 */
public class MainView {

    private static final String CONNECT = "Connect";
    private static final String DISCONNECT = "Disconnect";

    private Panel mainPanel;
    private DataPanel receivedDataPanel;
    private DataPanel sentDataPanel;
    private Button connectButton;
    private Button sendFileButton;
    private Button closeAppButton;
    private SendDataPanel sendDataPanel1;
    private SendDataPanel sendDataPanel2;
    private SendDataPanel sendDataPanel3;

    private final BasicWindow window;
    private final TerminalSize terminalSize;

    public MainView(BasicWindow window, TerminalSize terminalSize) {
        this.window = window;
        this.terminalSize = terminalSize;
    }

    public BasicWindow getWindow() {
        return window;
    }

    public void init(Component connectionSettingsPanel) {
        mainPanel = new Panel(new AbsoluteLayout());
        final TerminalSize prefferedMainPanelSize = getMaxPanelSizeForCurrentTerminalSize();
        mainPanel.setPreferredSize(prefferedMainPanelSize);
        final int halfPanelWidth = prefferedMainPanelSize.getColumns() / 2;
        final int halfPanelHeight = prefferedMainPanelSize.getRows() / 2;

        Border receivedDataPanelBorder = initReceivedDataPanel(halfPanelWidth, halfPanelHeight);

        initSentDataPanel(halfPanelHeight, halfPanelWidth);

        connectButton = new Button(CONNECT);
        sendFileButton = new Button("Send file");
        closeAppButton = new Button("Close app");

        Component buttonPanel = new Panel(new LinearLayout(Direction.HORIZONTAL).setSpacing(0))
                .addComponent(connectButton.withBorder(Borders.singleLineBevel()))
                .addComponent(sendFileButton.withBorder(Borders.singleLineBevel()))
                .addComponent(closeAppButton.withBorder(Borders.singleLineBevel()));

        buttonPanel.setSize(new TerminalSize(halfPanelWidth, buttonPanel.getPreferredSize().getRows()));
        setPositionRightOf(buttonPanel, receivedDataPanelBorder);
        mainPanel.addComponent(buttonPanel);

        initConnectionSettingsPanel(connectionSettingsPanel, buttonPanel);

        initSendDataPanels(halfPanelWidth, connectionSettingsPanel);

        window.setComponent(mainPanel);
    }

    private void initSendDataPanels(final int halfPanelWidth, Component connectionSettingsPanel) {
        Panel sendDataPanels = getSendDataPanels();
        TerminalSize sendDataPanelsPrefSize = sendDataPanels.getPreferredSize();
        sendDataPanels.setSize(new TerminalSize(halfPanelWidth, sendDataPanelsPrefSize.getRows()));
        setPositionBelowOf(sendDataPanels, connectionSettingsPanel);
        mainPanel.addComponent(sendDataPanels);
    }

    private void initConnectionSettingsPanel(Component connectionSettingsPanel, Component buttonPanel) {
        connectionSettingsPanel.setSize(connectionSettingsPanel.getPreferredSize());
        setPositionBelowOf(connectionSettingsPanel, buttonPanel);
        mainPanel.addComponent(connectionSettingsPanel);
    }

    private void initSentDataPanel(final int halfPanelHeight, final int halfPanelWidth) {
        Border sentDataPanelBorder = new DataPanel().withBorder(Borders.singleLine("Sent data"));
        sentDataPanelBorder.setPosition(new TerminalPosition(0, halfPanelHeight));
        sentDataPanelBorder.setSize(new TerminalSize(halfPanelWidth, halfPanelHeight));
        mainPanel.addComponent(sentDataPanelBorder);
        sentDataPanel = (DataPanel) sentDataPanelBorder.getComponent();
    }

    private Border initReceivedDataPanel(final int halfPanelWidth, final int halfPanelHeight) {
        Border receivedDataPanelBorder = new DataPanel().withBorder(Borders.singleLine("Received data"));
        receivedDataPanelBorder.setPosition(new TerminalPosition(0, 0));
        receivedDataPanelBorder.setSize(new TerminalSize(halfPanelWidth, halfPanelHeight));
        mainPanel.addComponent(receivedDataPanelBorder);
        receivedDataPanel = (DataPanel) receivedDataPanelBorder.getComponent();
        return receivedDataPanelBorder;
    }

    private TerminalSize getMaxPanelSizeForCurrentTerminalSize() {
        // for undecorated fullscreen window size will be equal to terminal size
        return terminalSize;
//        return new TerminalSize(
//                terminalSize.getColumns() - 5, terminalSize.getRows() - 4);
    }

    private Panel getSendDataPanels() {
        Panel multipleSendDataPanels = new Panel(new LinearLayout(Direction.VERTICAL));
        sendDataPanel1 = new SendDataPanel();
        sendDataPanel2 = new SendDataPanel();
        sendDataPanel3 = new SendDataPanel();
        multipleSendDataPanels.addComponent(sendDataPanel1, LinearLayout.createLayoutData(LinearLayout.Alignment.Fill));
        multipleSendDataPanels.addComponent(sendDataPanel2, LinearLayout.createLayoutData(LinearLayout.Alignment.Fill));
        multipleSendDataPanels.addComponent(sendDataPanel3, LinearLayout.createLayoutData(LinearLayout.Alignment.Fill));
        multipleSendDataPanels.setSize(multipleSendDataPanels.getPreferredSize());
        return multipleSendDataPanels;
    }

    public void setConnectButtonListener(Listener l) {
        connectButton.removeListener(l);
        connectButton.addListener(l);
    }

    public void setSendFileButtonListener(Listener l) {
        sendFileButton.removeListener(l);
        sendFileButton.addListener(l);
    }

    public void removeConnectButtonListener(Listener l) {
        connectButton.removeListener(l);
    }

    public void setCloseAppButtonListener(Listener l) {
        closeAppButton.removeListener(l);
        closeAppButton.addListener(l);
    }

    public void removeCloseAppButtonListener(Listener l) {
        closeAppButton.removeListener(l);
    }

    public void setReceivedData(String data) {
        receivedDataPanel.getDataTB().setText(data);
    }

    public void setSentData(String data) {
        sentDataPanel.getDataTB().setText(data);
    }

    public Button getConnectButton() {
        return connectButton;
    }

    public void setConnectButtonLabel() {
        connectButton.setLabel(CONNECT);
    }

    public void setDisconnectButtonLabel() {
        connectButton.setLabel(DISCONNECT);
    }

    public String getReceivedData() {
        return receivedDataPanel.getDataTB().getText();
    }

    public SendDataPanel getSendDataPanel1() {
        return sendDataPanel1;
    }

    public SendDataPanel getSendDataPanel2() {
        return sendDataPanel2;
    }

    public SendDataPanel getSendDataPanel3() {
        return sendDataPanel3;
    }

    public DataPanel getReceivedDataPanel() {
        return receivedDataPanel;
    }

    public DataPanel getSentDataPanel() {
        return sentDataPanel;
    }

    private Component prefferedSize(Component c) {
        c.setSize(c.getPreferredSize());
        return c;
    }

    private void setPositionRightOf(Component right, Component left) {
        TerminalSize leftSize = left.getSize();
        TerminalPosition leftPosition = left.getPosition();
        right.setPosition(new TerminalPosition(
                leftPosition.getColumn() + leftSize.getColumns(),
                leftPosition.getRow()
        ));
    }

    private void setPositionBelowOf(Component below, Component up) {
        TerminalSize upSize = up.getSize();
        TerminalPosition upPosition = up.getPosition();
        below.setPosition(new TerminalPosition(
                upPosition.getColumn(),
                upPosition.getRow() + upSize.getRows()
        ));
    }

}
